package com.jdapp.ddddddd.activity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.R;
import com.jdapp.ddddddd.model.FileInfo;
import com.jdapp.ddddddd.utils.Http;
import com.jdapp.ddddddd.utils.ThumbWriter;
import com.jdapp.ddddddd.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

public class ImageViewActivity extends Activity {

    protected static final String TAG = "ImageBoxActivity";

    private DynamicZoomControl mZoomControl;
    private ImageZoomView mZoomView;
    private ZoomViewOnTouchListener mZoomListener;
    private ArrayList<String> imgUrls;
    private int currentPage;
    private DiskLruCache cache;
    private ProgressBar progressCircle;
    private HashMap<String, Boolean> downloading;
    private boolean needThumb;
    private int screenWidth;
    private int screenHight;
    private String fileId;
    private String FileName;
    private boolean safememoMode;
    private boolean noSampling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_imageview);
        progressCircle = (ProgressBar) findViewById(R.id.progress_circle);

        FileInfo fileInfo = this.getIntent().getExtras()
                .getParcelable(App.bundleKeyFileinfo);
        fileId = fileInfo.getId();
        FileName = fileInfo.getName();
        needThumb = this.getIntent().getExtras().getBoolean("NEED_THUMB");
        try {
            cache = DiskLruCache
                    .open(App.APP_CACHE_DIR, 1, 1, 30 * 1024 * 1024);
        } catch (IOException e) {
            Toast.makeText(this, "init diskcache error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        if (null == App.sessionBaiduPan) {
            Toast.makeText(this, "baidu cookie is empty,check it",
                    Toast.LENGTH_LONG).show();
        }
        Http.setCookie(App.sessionBaiduPan);
        // load conf if exist
        SharedPreferences cur = getPreferences(MODE_PRIVATE);
        //
        currentPage = cur.getInt(fileInfo.getId(), 0);
        imgUrls = Utils.getUrls(fileInfo);

        mZoomControl = new DynamicZoomControl();
        mZoomListener = new ZoomViewOnTouchListener(getApplicationContext()) {

            @Override
            public void onNextPage() {
                if (currentPage + 1 >= imgUrls.size()){
                    Toast.makeText(ImageViewActivity.this, "last page", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentPage++;
                if (imgUrls.size() <= currentPage)
                    currentPage = imgUrls.size() - 1;
                loadCurrentPage();
            }

            @Override
            public void onPrevPage() {
                if (currentPage - 1 < 0){
                    Toast.makeText(ImageViewActivity.this, "first page", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentPage--;
                if (0 > currentPage)
                    currentPage = 0;
                loadCurrentPage();
            }

            @Override
            public void onSelectPage() {
                LayoutInflater inflater = LayoutInflater
                        .from(ImageViewActivity.this);
                View promptView = inflater.inflate(R.layout.seekbar_dialog,
                        null);
                final TextView tv1 = (TextView) promptView
                        .findViewById(R.id.seekbar_dialog_tv1);
                final SeekBar mSeekbar = (SeekBar) promptView
                        .findViewById(R.id.seekbar_dialog_seekBar1);
                tv1.setText(currentPage + " / " + (imgUrls.size() - 1));
                mSeekbar.setMax(imgUrls.size() - 1);
                mSeekbar.setProgress(currentPage);
                mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progress, boolean fromUser) {
                        tv1.setText(progress + " / " + (imgUrls.size() - 1));

                    }
                });
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ImageViewActivity.this);
                alertDialogBuilder
                        .setView(promptView)
                        .setTitle(FileName)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        if (currentPage != mSeekbar.getProgress()){
                                            currentPage = mSeekbar.getProgress();
                                            loadCurrentPage();
                                        }
                                    }
                                }).create().show();
                return;
            }

        };
        mZoomListener.setZoomControl(mZoomControl);
        mZoomListener.setFlingable(false);

        mZoomView = (ImageZoomView) findViewById(R.id.mivPage);
        mZoomView.setZoomState(mZoomControl.getZoomState());
        mZoomView.setImage(BitmapFactory.decodeResource(getResources(),
                R.drawable.hehe));
        mZoomView.setOnTouchListener(mZoomListener);
        mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());
        mZoomView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        resetZoomState();
                    }
                });
        downloading = new HashMap<String, Boolean>();
        safememoMode = App.getSharedPreferences().getBoolean(SettingsActivity.KEY_PREF_SAFE_MEMORY, false);
        noSampling = App.getSharedPreferences().getBoolean(SettingsActivity.KEY_PREF_NOSAMPLING, false);

    }

    @Override
    protected void onResume() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHight = metrics.heightPixels;
        loadCurrentPage();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }
    

    @Override
    protected void onPause() {
        super.onPause();
        mZoomView.setImage(null);
        Http.cancelAll(this);
        SharedPreferences cur = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = cur.edit();
        editor.putInt(fileId, currentPage);
        editor.commit();
    }

    private void resetZoomState() {
        Log.d(TAG, "ZoomView Width: " + mZoomView.getWidth());
        Log.d(TAG, "ZoomView Height: " + mZoomView.getHeight());
        Log.d(TAG, "AspectQuotient: " + mZoomView.getAspectQuotient().get());

        mZoomControl.getZoomState().setAlignX(AlignX.Center);
        mZoomControl.getZoomState().setAlignY(AlignY.Top);
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.0f);
        mZoomControl.getZoomState().setDefaultZoom(1f);
        mZoomControl.getZoomState().notifyObservers();
    }

    /**
     * 首先检查缓存,没有则添加下载任务
     * 
     * @param
     */
    private void loadCurrentPage() {
        int index = pageIndexChecker(currentPage);
        final String downloadUrl = imgUrls.get(index);
            if (cache.exist(Utils.md5(downloadUrl))) {
                if (safememoMode){
                    mZoomView.setImage(null);
                }
                new loadImageTask().execute(downloadUrl);
            } 
            else if (downloading.get(downloadUrl) != null && downloading.get(downloadUrl) == true ){
                if (progressCircle.getVisibility() != View.VISIBLE)
                    progressCircle.setVisibility(View.VISIBLE);
            }
            else {
                if (downloading.get(downloadUrl) == null
                        || downloading.get(downloadUrl) == false)
                    downloadImgData(downloadUrl);
            }

    }

    private void notifyCurrentIsReady() {
        loadNextTwoPage();
    }

    private void loadNextTwoPage() {
        final String l1 = imgUrls.get(pageIndexChecker(currentPage + 1));
        final String l2 = imgUrls.get(pageIndexChecker(currentPage + 2));
        prepareData(l1);
        prepareData(l2);
    }

    private void prepareData(String urlKey) {
        boolean isdownloading = false;
        if (downloading.get(urlKey)!= null && downloading.get(urlKey) == true)
            isdownloading = true;
        if (isdownloading)
            return;
        if (!cache.exist(Utils.md5(urlKey))) {
            downloadImgData(urlKey);
        }

    }

    private int pageIndexChecker(int index) {
        if (index < 0)
            return 0;
        else if (index >= imgUrls.size())
            return imgUrls.size() - 1;
        else
            return index;
    }

    private void downloadImgData(final String downloadUrl) {
        Http.get(this, downloadUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.d(TAG, "start mession: " + getRequestURI());
                downloading.put(downloadUrl, true);
                if (imgUrls.get(currentPage).equals(getRequestURI().toString())) {
                    // current page is been loading,show progress circle
                    if (progressCircle.getVisibility() != View.VISIBLE) {
                        progressCircle.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onFinish() {
                downloading.put(getRequestURI().toString(), false);
                if (imgUrls.get(currentPage).equals(getRequestURI().toString())) {
                    if (progressCircle.getVisibility() != View.GONE) {
                        progressCircle.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                    byte[] responseBody) {
                String urlStr = getRequestURI().toString();
                Log.d(TAG, "onSuccess: " + urlStr);
                try {
                    Editor editor = cache.edit(Utils.md5(urlStr));
                    OutputStream out = editor.newOutputStream(0);
                    out.write(responseBody);
                    out.flush();
                    editor.commit();
                    cache.flush();
                    final String curUrl = imgUrls.get(currentPage);
                    if (curUrl.equals(downloadUrl)) {
                        loadCurrentPage();
                    } else {
                        prepareData(downloadUrl);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                    byte[] responseBody, Throwable error) {
                String response;
                try {
                    response = responseBody == null ? "" : new String(
                            responseBody, getCharset());
                } catch (UnsupportedEncodingException e) {
                    response = "";
                }
                final String curUrl = imgUrls.get(currentPage);
                if (curUrl.equals(downloadUrl)) {
                    Toast.makeText(
                            ImageViewActivity.this,
                            "download failed, code:" + statusCode + " msg: "
                                    + response, Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    /**
     * To define the inSampleSize dynamically, you may want to know the image
     * size to take your decision:
     * http://stackoverflow.com/questions/11820266/android
     * -bitmapfactory-decodestream-out-of-memory-with-a-400kb-file-with-2mb-f
     **/
    private static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d("calculateInSampleSize", String.format("SampleSize:%d height:%d width:%d reqHeight:%d reqWidth:%d", 
                inSampleSize,height,width,reqHeight,reqWidth));
        return inSampleSize;
    }
    
    //#################async tasks#################################################################
    
    class loadImageTask extends AsyncTask<String, Float, Bitmap> {
        @Override
        protected void onPreExecute() {
            if (progressCircle.getVisibility() != View.VISIBLE){
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        progressCircle.setVisibility(View.VISIBLE);
                    }
                });
            }
            super.onPreExecute();
        }
        
        @Override
        protected Bitmap doInBackground(String... params) {
            String key = Utils.md5(params[0]);
            Snapshot shot = null;
            try {
                shot = cache.get(key);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            if (shot == null) return null;
            // First decode with inJustDecodeBounds=true to check dimensions
            BufferedInputStream is = new BufferedInputStream(shot.getInputStream(0));
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);
            shot.close();
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Calculate inSampleSize
            if (noSampling)
                opts.inSampleSize = 1;
            else
                opts.inSampleSize = calculateInSampleSize(opts,
                        screenWidth, screenHight);
            // Decode bitmap with inSampleSize set
            opts.inJustDecodeBounds = false;
            Snapshot shot2 = null;
            try {
                shot2 = cache.get(key);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (shot2 == null) return null;
            BufferedInputStream is2 = new BufferedInputStream(shot2.getInputStream(0));
            Bitmap bitmap = BitmapFactory.decodeStream(is2, null, opts);
            shot2.close();
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mZoomView.setImage(result);
                if (needThumb) {
                    new ThumbWriter(fileId).execute(result);
                    needThumb = false;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Read File Failed :<", Toast.LENGTH_LONG).show();
            }
            if (progressCircle.getVisibility() == View.VISIBLE) {
                progressCircle.setVisibility(View.GONE);
            }
            notifyCurrentIsReady();
            super.onPostExecute(result);
        }

    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}

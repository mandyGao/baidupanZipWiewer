package com.jdapp.ddddddd.activity;

import java.io.IOException;
import java.io.InputStream;
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
import android.os.Bundle;
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
import com.jdapp.ddddddd.App.ZoomMode;
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

    private ZoomMode mZoomMode = ZoomMode.FIT_SCREEN;
    protected static final String TAG = "ImageBoxActivity";

    private DynamicZoomControl mZoomControl;
    private ImageZoomView mZoomView;
    private Bitmap mBitmap;
    private ZoomViewOnTouchListener mZoomListener;
    private FileInfo fileInfo;
    private ArrayList<String> imgUrls;
    private int currentPage;
    private DiskLruCache cache;
    private ProgressBar progressCircle;
    private HashMap<String, Boolean> downloading;
    private boolean needThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_imageview);
        progressCircle = (ProgressBar) findViewById(R.id.progress_circle);

        fileInfo = this.getIntent().getExtras()
                .getParcelable(App.bundleKeyFileinfo);
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
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hehe);
        mZoomListener = new ZoomViewOnTouchListener(getApplicationContext()) {

            @Override
            public void onNextPage() {
                currentPage++;
                if (imgUrls.size() <= currentPage)
                    currentPage = imgUrls.size() - 1;
                loadCurrentPage();
            }

            @Override
            public void onPrevPage() {
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
                        .setTitle(fileInfo.getName())
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        currentPage = mSeekbar.getProgress();
                                        loadCurrentPage();
                                    }
                                }).create().show();
                return;
            }

        };
        mZoomListener.setZoomControl(mZoomControl);
        mZoomListener.setFlingable(false);

        mZoomView = (ImageZoomView) findViewById(R.id.mivPage);
        mZoomView.setZoomState(mZoomControl.getZoomState());
        mZoomView.setImage(mBitmap);
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
        loadCurrentPage();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        SharedPreferences cur = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = cur.edit();
        editor.putInt(fileInfo.getId(), currentPage);
        editor.commit();
        Http.cancelAll(this);
    }

    private void resetZoomState() {
        Log.d(TAG, "ZoomView Width: " + mZoomView.getWidth());
        Log.d(TAG, "ZoomView Height: " + mZoomView.getHeight());
        Log.d(TAG, "AspectQuotient: " + mZoomView.getAspectQuotient().get());

        // mZoomControl.getZoomState().setAlignX(AlignX.Right);
        mZoomControl.getZoomState().setAlignX(AlignX.Center);
        mZoomControl.getZoomState().setAlignY(AlignY.Top);
        mZoomControl.getZoomState().setPanX(0.5f);
        mZoomControl.getZoomState().setPanY(0.0f);
        // mZoomControl.getZoomState().setZoom(2f);
        mZoomControl.getZoomState().setDefaultZoom(
                computeDefaultZoom(mZoomMode, mZoomView, mBitmap));
        mZoomControl.getZoomState().notifyObservers();
    }

    private float computeDefaultZoom(ZoomMode mode, ImageZoomView view,
            Bitmap bitmap) {
        if (view.getAspectQuotient() == null
                || view.getAspectQuotient().get() == Float.NaN) {
            return 1f;
        }
        if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
            return 1f;
        }
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            return 1f;
        }

        if (mode == ZoomMode.FIT_SCREEN) {
            return 1f;
        }

        // aq = (bW / bH) / (vW / vH)
        float aq = view.getAspectQuotient().get();
        float zoom = 1f;

        if (mode == ZoomMode.FIT_WIDTH || mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
            // Over height
            if (aq < 1f) {
                zoom = 1f / aq;
            } else {
                zoom = 1f;
            }

            if (mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
                if (1f * bitmap.getWidth() / view.getWidth() > 1.5f
                        && bitmap.getWidth() > bitmap.getHeight()) {
                    zoom *= (2f + App.WIDTH_AUTO_SPLIT_MARGIN)
                            / (1f + App.WIDTH_AUTO_SPLIT_MARGIN);
                }
            }
        } else if (mode == ZoomMode.FIT_HEIGHT) {
            // Over width
            if (aq > 1f) {
                zoom = aq;
            } else {
                zoom = 1f;
            }
        }

        return zoom;
    }

    /**
     * 首先检查缓存,没有则添加下载任务
     * 
     * @param
     */
    private void loadCurrentPage() {
        int index = pageIndexChecker(currentPage);
        final String downloadUrl = imgUrls.get(index);
        try {
            Snapshot snapshot = cache.get(Utils.md5(downloadUrl));
            if (snapshot != null) {
                consumeData(snapshot);
                snapshot.close();
                notifyCurrentIsReady();
            } else {
                if (downloading.get(downloadUrl) == null
                        || downloading.get(downloadUrl) == false)
                    downloadImgData(downloadUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        if (downloading.get(urlKey) == null){
            downloadImgData(urlKey);
            return;
        }
        if (downloading.get(urlKey) == true)
            return;
        if (downloading.get(urlKey) == false){
            try {
                Snapshot snapshot = cache.get(Utils.md5(urlKey));
                if (snapshot == null) {
                    downloadImgData(urlKey);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

    private void consumeData(Snapshot shot) {
        if (progressCircle.getVisibility() == View.VISIBLE) {
            progressCircle.setVisibility(View.GONE);
        }
        InputStream is = shot.getInputStream(0);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        long len = shot.getLength(0);
        if (len > 1024 * 1024) {
            Log.d(TAG, "scaled:" + 2);
            opts.inSampleSize = 2;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
        mZoomView.setImage(bitmap);
        if (needThumb){
            new ThumbWriter(fileInfo.getId()).execute(bitmap);
            needThumb = false;
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
                    // TODO Auto-generated catch block
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

}

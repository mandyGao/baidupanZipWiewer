package hotstu.github.bdzviewer;

import hotstu.github.bdzviewer.baiduapi.AsyncImageLoader;
import hotstu.github.bdzviewer.baiduapi.ImageLoader.OnImageloadListener;
import hotstu.github.bdzviewer.baiduapi.LoadingQuene;
import hotstu.github.bdzviewer.model.FileInfo;
import hotstu.github.bdzviewer.utils.FileUtil;
import hotstu.github.bdzviewer.utils.IoUtils;
import hotstu.github.bdzviewer.utils.ThumbWriter;
import hotstu.github.bdzviewer.utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

public class MoImageViewActiviy extends Activity implements Observer {

    protected static final String TAG = "ImageBoxActivity";

    private DynamicZoomControl mZoomControl;
    private ImageZoomView mZoomView;
    private ZoomViewOnTouchListener mZoomListener;
    private List<String> urls;
    private int currentPage;
    private DiskLruCache cache;
    private ProgressBar progressCircle;
    private boolean needThumb;
    private int screenWidth;
    private int screenHight;
    private String fileId;
    private String FileName;
    private boolean safememoMode;
    private boolean noSampling;
    private LoadingQuene mLoadingQuene;
    private loadingMgr mLoadingMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            cache = FileUtil.diskCacheInstance();
        } catch (IOException e) {
            Toast.makeText(this, "init diskcache error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        if (null == App.sessionBaiduPan) {
            Toast.makeText(this, "baidu cookie is empty,check it",
                    Toast.LENGTH_LONG).show();
        }
        // load conf if exist
        SharedPreferences cur = getSharedPreferences(App.PROGRESS_RECORD_NAME,
                Context.MODE_PRIVATE);
        //
        currentPage = cur.getInt(fileInfo.getId(), 0);
        urls = Utils.getUrls(fileInfo);

        mZoomControl = new DynamicZoomControl();
        mZoomListener = new ZoomViewOnTouchListener(getApplicationContext()) {

            @Override
            public void onNextPage() {
                if (currentPage + 1 >= urls.size()) {
                    Toast.makeText(MoImageViewActiviy.this, "last page",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                currentPage++;
                if (urls.size() <= currentPage)
                    currentPage = urls.size() - 1;
                loadCurrentPage();
            }

            @Override
            public void onPrevPage() {
                if (currentPage - 1 < 0) {
                    Toast.makeText(MoImageViewActiviy.this, "first page",
                            Toast.LENGTH_SHORT).show();
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
                        .from(MoImageViewActiviy.this);
                View promptView = inflater.inflate(R.layout.seekbar_dialog,
                        null);
                final TextView tv1 = (TextView) promptView
                        .findViewById(R.id.seekbar_dialog_tv1);
                final SeekBar mSeekbar = (SeekBar) promptView
                        .findViewById(R.id.seekbar_dialog_seekBar1);
                tv1.setText((currentPage + 1) + " / " + (urls.size()));
                mSeekbar.setMax(urls.size() - 1);
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
                        tv1.setText((progress + 1) + " / " + (urls.size()));

                    }
                });
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MoImageViewActiviy.this);
                alertDialogBuilder
                        .setView(promptView)
                        .setTitle(FileName)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        if (currentPage != mSeekbar
                                                .getProgress()) {
                                            currentPage = mSeekbar
                                                    .getProgress();
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
        safememoMode = App.getSharedPreferences().getBoolean(
                SettingsActivity.KEY_PREF_SAFE_MEMORY, false);
        noSampling = App.getSharedPreferences().getBoolean(
                SettingsActivity.KEY_PREF_NOSAMPLING, false);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHight = metrics.heightPixels;
        mLoadingQuene = LoadingQuene.instance();
        mLoadingMgr = new loadingMgr();

    }

    @Override
    protected void onResume() {
        cache.addObserver(this);
        mLoadingQuene.addObserver(mLoadingMgr);
        loadCurrentPage();
        super.onResume();
    }

    private void loadCurrentPage() {
        final int index = pageIndexChecker(currentPage);
        final String downloadUrl = urls.get(index);
        if (progressCircle.getVisibility() != View.VISIBLE)
            progressCircle.setVisibility(View.VISIBLE);
        if (cache.exist(Utils.md5(downloadUrl))) {
            new loadImageTask().execute(downloadUrl);
        } else {
            mLoadingQuene.enquene(downloadUrl);
        }

    }

    public void onCurrentReady() {
        if (!cache
                .equals(Utils.md5(urls.get(pageIndexChecker(currentPage + 1)))))
            mLoadingQuene.enquene(urls.get(pageIndexChecker(currentPage + 1)));
        if (!cache
                .equals(Utils.md5(urls.get(pageIndexChecker(currentPage + 2)))))
            mLoadingQuene.enquene(urls.get(pageIndexChecker(currentPage + 2)));

    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
            int _reqWidth, int _reqHeight) {

        // Raw height and width of image
        int reqWidth = _reqWidth > 768 ? _reqWidth : 768;
        int reqHeight = _reqHeight > 1024 ? _reqHeight : 1024;
        // 1024*768*4byte
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

        Log.d("calculateInSampleSize", String.format(
                "SampleSize:%d height:%d width:%d reqHeight:%d reqWidth:%d",
                inSampleSize, height, width, reqHeight, reqWidth));
        return inSampleSize;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // mZoomView.setImage(null);
        SharedPreferences cur = getSharedPreferences(App.PROGRESS_RECORD_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cur.edit();
        editor.putInt(fileId, currentPage);
        editor.commit();
        cache.deleteObserver(this);
        mLoadingQuene.deleteObserver(mLoadingMgr);
        mLoadingMgr.cancelAll();
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

    private int pageIndexChecker(int index) {
        if (index < 0)
            return 0;
        else if (index >= urls.size())
            return urls.size() - 1;
        else
            return index;
    }

    @Override
    public void update(Observable observable, Object data) {
        System.out.println(String.valueOf(data));
        if (this.urls.get(currentPage).equals(data)) {
            loadCurrentPage();
        }

    }

    // #############loadingMgr##########################################

    private class loadingMgr implements Observer, OnImageloadListener {
        private Map<String, AsyncImageLoader> tasks = new HashMap<String, AsyncImageLoader>();

        public void cancelAll() {
            for (Entry<String, AsyncImageLoader> element : tasks.entrySet()) {
                element.getValue().cancel(true);
            }
            tasks.clear();
        }

        @Override
        public void update(Observable observable, Object data) {
            LoadingQuene quene = (LoadingQuene) observable;
            ArrayList<String> removeList = new ArrayList<String>();
            for (String key : tasks.keySet()) {
                // 清除掉队首未完成或已完成的任务
                if (!quene.getUrls().contains(key)) {
                    tasks.get(key).cancel(true);
                    removeList.add(key);
                }
            }
            for (String key : removeList) {
                tasks.remove(key);
            }
            for (String key : quene.getUrls()) {
                // 添加新任务
                if (!tasks.keySet().contains(key)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("url", key);
                    AsyncImageLoader task = new AsyncImageLoader(bundle, this);
                    tasks.put(key, task);
                    task.execute();
                }
            }

        }

        @Override
        public void onStart(String msg) {
            System.out.println("start:" + msg);

        }

        @Override
        public void onProgress(long completed, long total) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSuccess() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFailed() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFinish(String msg) {
            System.out.println("finished:" + msg);

        }

    }

    // #############loadImageTask#######################################
    class loadImageTask extends AsyncTask<String, Float, Bitmap> {
        @Override
        protected void onPreExecute() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (safememoMode) {
                        mZoomView.setImage(null);
                    }
                    if (progressCircle.getVisibility() != View.VISIBLE) {
                        progressCircle.setVisibility(View.VISIBLE);
                    }
                }
            });

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String key = Utils.md5(params[0]);
            Bitmap bitmap = null;
            try {
                bitmap = cacheLoader(key);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bitmap;

        }

        protected Bitmap cacheLoader(String key) throws IOException {

            Snapshot shot = null;
            InputStream is = null;
            try {
                shot = cache.get(key);
                if (shot == null)
                    return null;
                is = new BufferedInputStream(shot.getInputStream(0));
                if (is.markSupported()) {

                    is.mark(1024);

                }
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, opts);
                if (opts.outHeight < 0) {
                    // Invalid image file
                    IoUtils.closeSilently(is);
                    shot.close();
                    cache.remove(key);
                    return null;
                }
                if (noSampling)
                    opts.inSampleSize = 1;
                else
                    opts.inSampleSize = calculateInSampleSize(opts,
                            screenWidth, screenHight);
                try {
                    is.reset();
                } catch (IOException e) {
                    IoUtils.closeSilently(is);
                    shot.close();
                    shot = cache.get(key);
                    is = new BufferedInputStream(shot.getInputStream(0));
                }
                opts.inJustDecodeBounds = false;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeStream(is, null, opts);
            } finally {
                IoUtils.closeSilently(is);
                if (shot != null) {
                    shot.close();
                }
            }

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                mZoomView.setImage(result);
                onCurrentReady();
                if (needThumb) {
                    new ThumbWriter(fileId).execute(result);
                    App.mainlistNeedReload = true;
                    needThumb = false;
                }
            } else {
                Toast.makeText(getApplicationContext(), "百度抽风，不是图片文件",
                        Toast.LENGTH_LONG).show();
            }
            if (progressCircle.getVisibility() == View.VISIBLE) {
                progressCircle.setVisibility(View.GONE);
            }
        }

    }

}

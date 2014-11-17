package hotstu.github.bdzviewer.baiduapi;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.utils.FileUtil;
import hotstu.github.bdzviewer.utils.HttpUtil;
import hotstu.github.bdzviewer.utils.IoUtils;
import hotstu.github.bdzviewer.utils.IoUtils.CopyListener;
import hotstu.github.bdzviewer.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;

import android.os.Bundle;

import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * 下载图片的异步类, 图片直接保存到LRUCache,通过接口回调返回event
 * 
 * @author foo
 * 
 */
public class AsyncImageLoader extends ImageLoader {

    private final String targetUrl;
    private final String ua;
    private final String cookie;
    private final String referer;
    private final OnImageloadListener mListener;


    /**
     * 
     * @param bundle
     *            必须包含url, 可选包含ua cookie referer
     */
    public AsyncImageLoader(Bundle bundle, 
            OnImageloadListener listener) {
        super();
        if (bundle.getString("url") == null)
            throw new IllegalArgumentException("can't find a url in bundle");
        targetUrl = bundle.getString("url");
        ua = bundle.getString("ua") == null ? HttpUtil.UA_FIREFOX : bundle
                .getString("ua");
        cookie = bundle.getString("cookie") == null ? App.sessionBaiduPan
                : bundle.getString("cookie");
        referer = bundle.getString("referer") == null ? HttpUtil.REFERER_DEFAULT
                : bundle.getString("referer");
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = false;
        try {
            result = downloader();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStart(this.targetUrl);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if (result) {
            //disk cache commited
            try {
                FileUtil.diskCacheInstance().notifyObservers(this.targetUrl);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mListener.onFinish(targetUrl);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Boolean result) {
        // TODO Auto-generated method stub
        super.onCancelled(result);
        mListener.onFinish(targetUrl);
    }

    

    @Override
    protected boolean downloader() throws IOException {
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        //CookieManager cm = HttpUtil.getCookieMgrinstance();
        //client.setCookieHandler(cm);
        Request req = new Request.Builder().url(this.targetUrl)
                .addHeader("Referer", this.referer)
                .addHeader("User-Agent", this.ua)
                .addHeader("Cookie", this.cookie)
                .build();
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);
        if (!resp.isSuccessful()) {
            return false;
        }
        
        boolean saved = false;
       
        InputStream inputStream = resp.body().byteStream();
        try {
            saved = save(this.targetUrl, inputStream, new CopyListener() {
                
                @Override
                public boolean onBytesCopied(int current, int total) {
                    if (isCancelled())
                        return false;
                    else
                        return true;
                }
            });
        } finally {
            IoUtils.closeSilently(inputStream);
        }
        return saved;
        
    }
    
    private boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        DiskLruCache.Editor editor = FileUtil.diskCacheInstance().edit(Utils.md5(imageUri));
        if (editor == null) {
            return false;
        }

        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), 1024*4);
        boolean copied = false;
        try {
            copied = IoUtils.copyStream(imageStream, os, listener, 1024*4);
        } finally {
            IoUtils.closeSilently(os);
            if (copied) {
                editor.commit();
            } else {
                editor.abort();
            }
        }
        return copied;
    }


}

package hotstu.github.bdzviewer.baiduapi;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.utils.HttpUtil;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

/**
 * 下载图片的异步类, 图片直接保存到LRUCache,通过接口回调返回event
 * @author foo
 *
 */
public class AsyncImageLoader extends AsyncTaskLoader<String> {
    private final String targetUrl;
    private final String ua;
    private final String cookie;
    private final String referer;
    //private final int catagegy;

    /**
     * 
     * @param context
     * @param bundle 必须包含url, 可选包含ua cookie referer
     */
    public AsyncImageLoader(Context context, Bundle bundle) {
        super(context);
        if (bundle.getString("url") == null)
            throw new IllegalArgumentException("can't find a url in bundle");
        targetUrl = bundle.getString("url");
        ua = bundle.getString("ua") == null ? HttpUtil.UA_FIREFOX : bundle.getString("ua");
        cookie = bundle.getString("cookie") == null ? App.sessionBaiduPan : bundle.getString("cookie");
        referer = bundle.getString("referer") == null ? HttpUtil.REFERER_DEFAULT : bundle.getString("referer");
        
    }

    @Override
    public String loadInBackground() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 这里可以做缓存
     */
    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
    }

    @Override
    public void onCanceled(String data) {
        // TODO Auto-generated method stub
        super.onCanceled(data);
    }

    @Override
    public void stopLoading() {
        // TODO Auto-generated method stub
        super.stopLoading();
    }

    @Override
    protected void onForceLoad() {
        // TODO Auto-generated method stub
        super.onForceLoad();
    }

    @Override
    protected String onLoadInBackground() {
        return loadInBackground();
    }

    /**
     * 这里要执行forceLoad方法
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * 这里要显示调用cancelLoad()
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onAbandon() {
        // TODO Auto-generated method stub
        super.onAbandon();
    }

    @Override
    protected void onReset() {
        cancelLoad();
    }

}

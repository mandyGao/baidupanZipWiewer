package hotstu.github.bdzviewer.baiduapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public abstract class ImageLoader extends AsyncTask<Void, String, Boolean> {
    
    public interface OnImageloadListener {
        public void onStart(String msg);
        public void onProgress(long completed, long total);
        public void onSuccess();
        public void onFailed();
        public void onFinish(String msg);
    }
    
    /**
     * 
     * @param url
     * @return true 下载并写入diskcache成功，false other 
     * @throws IOException 
     */
    protected abstract boolean downloader() throws IOException ;
    
    //protected abstract Bitmap prosscer(InputStream is);
    
}

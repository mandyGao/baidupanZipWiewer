package hotstu.github.bdzviewer.utils;

import hotstu.github.bdzviewer.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;

public class ThumbWriter extends AsyncTask<Bitmap, Void, Void> {
    String fileName;
    
    public ThumbWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        if (params[0] == null) {
            return null;
        }

        float scale;
        if (params[0].getWidth() < params[0].getHeight()) {
            scale = 96f / (float) params[0].getWidth();
        } else {
            scale = 96f / (float) params[0].getHeight();
        }
        int width = (int)(params[0].getWidth()*scale);
        int hight = (int)(params[0].getHeight()*scale);
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(params[0], width, hight);
        
        try {
            OutputStream os = new FileOutputStream(new File(App.APP_THUMB_DIR, fileName));
            thumbnail.compress(CompressFormat.PNG, 90, os);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            Log.d("ThumbWriter", "file not fount:"+thumbnail);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }


}

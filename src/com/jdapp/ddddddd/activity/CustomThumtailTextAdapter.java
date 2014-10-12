package com.jdapp.ddddddd.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.R;
import com.jdapp.ddddddd.model.FileInfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomThumtailTextAdapter extends BaseAdapter {
    
    private class ThumbnailLoader extends AsyncTask<ImageView, Void, Bitmap> {
        ImageView view;
        String tag;

        @Override
        protected Bitmap doInBackground(ImageView... params) {
            view = params[0];
            tag = (String)view.getTag();
            File thrumbFile = new File(App.APP_THUMB_DIR, tag);
            if (!thrumbFile.exists()) return null;
            InputStream in = null;
            Bitmap bitmap = null;
            try {
                in = new FileInputStream(thrumbFile);
                bitmap = BitmapFactory.decodeStream(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if ( in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return bitmap;
           
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                view.setImageBitmap(result);
                view.setTag(R.id.TAG_NEED_THUMB, false);
                thumbAvalibledict.put(tag, result);
            } else {
                view.setTag(R.id.TAG_NEED_THUMB, true);
                thumbAvalibledict.put(tag, defoult);
                super.onPostExecute(result);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }
    
    
    Activity activity;
    LayoutInflater inflater;
    List<FileInfo> fileItems;
    HashMap<String,Bitmap> thumbAvalibledict;
    Bitmap defoult;

    public CustomThumtailTextAdapter(Activity activity, List<FileInfo> _fileItems) {
        this.activity = activity;
        this.fileItems = new ArrayList<FileInfo>();
        for (FileInfo f : _fileItems) {
            this.fileItems.add(f);
        }
        this.thumbAvalibledict = new HashMap<String, Bitmap>();
        this.defoult = BitmapFactory.decodeResource(activity.getResources(), R.drawable.unknown_image_icon);
    }
    
    public List<FileInfo> getFileItems() {
        return fileItems;
    }

    public void setFileItems(List<FileInfo> _fileItems) {
        this.fileItems.clear();
        for (FileInfo f : _fileItems) {
            this.fileItems.add(f);
        }
    }

    public HashMap<String, Bitmap> getThumbAvalibledict() {
        return thumbAvalibledict;
    }

    @Override
    public int getCount() {
        return fileItems.size();
    }

    @Override
    public Object getItem(int position) {
        return fileItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item_icon_text, null);
        final ImageView iv = (ImageView) convertView.findViewById(R.id.thumbnail);
        final TextView title = (TextView) convertView.findViewById(R.id.title);
        FileInfo f = fileItems.get(position);
        title.setText(f.getName());
        iv.setTag(f.getId());
        Log.d("ThumtailAdapter_getView", position+": "+f.getName()+" tag:"+(String)iv.getTag());
        if (thumbAvalibledict.get(f.getId()) == null)
            new ThumbnailLoader().execute(iv);
        else {
            iv.setImageBitmap(thumbAvalibledict.get(f.getId()));
            if (thumbAvalibledict.get(f.getId()).equals(defoult)){
                iv.setTag(R.id.TAG_NEED_THUMB, true);
            } else{
                iv.setTag(R.id.TAG_NEED_THUMB, false);
            }
        }
        return convertView;
    }

}

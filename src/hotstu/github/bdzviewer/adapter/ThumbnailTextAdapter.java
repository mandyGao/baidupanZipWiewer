package hotstu.github.bdzviewer.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hotstu.github.bdzviewer.App;
import hotstu.github.bdzviewer.R;
import hotstu.github.bdzviewer.model.FileInfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThumbnailTextAdapter extends BaseAdapter {
    
    private class ThumbnailLoader extends AsyncTask<String, Void, Bitmap> {
        private int positon;
        private ViewHolder holder;

        public ThumbnailLoader(int position, ViewHolder holder) {
            this.positon = position;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            File thrumbFile = new File(App.APP_THUMB_DIR, params[0]);
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
                if (this.positon != holder.position ) return;//positon is changed, that means the view is recycled
                if ( result != null) {
                    holder.iv.setImageBitmap(result);
                    holder.iv.setTag(R.id.TAG_NEED_THUMB, false);
                } else {
                    holder.iv.setImageBitmap(defaultImg);
                    holder.iv.setTag(R.id.TAG_NEED_THUMB, true);
                    //thumbAvalibleDict.put(tag, defaultImg);
                }
            }
           

    }
    
    
    Activity activity;
    LayoutInflater inflater;
    List<FileInfo> fileItems;
    SharedPreferences pref;
    Bitmap defaultImg;

    public ThumbnailTextAdapter(Activity activity, List<FileInfo> _fileItems) {
        this.activity = activity;
        this.fileItems = new ArrayList<FileInfo>();
        for (FileInfo f : _fileItems) {
            this.fileItems.add(f);
        }
        this.defaultImg = BitmapFactory.decodeResource(activity.getResources(), R.drawable.unknown_image_icon);
        pref = activity.getSharedPreferences(App.PROGRESS_RECORD_NAME, Context.MODE_PRIVATE);
    }
    
    public List<FileInfo> getFileItems() {
        return fileItems;
    }
    
    public void clear() {
        fileItems.clear();
    }
    
    public void addAll(List<FileInfo> fs) {
        for (FileInfo f : fs) {
            fileItems.add(f);
        }
    }

    public void setFileItems(List<FileInfo> _fileItems) {
        this.fileItems.clear();
        for (FileInfo f : _fileItems) {
            this.fileItems.add(f);
        }
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
        //performance-tips-for-androids-listview
        //http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        ViewHolder holder;
        if (convertView == null) {
            //convertView = inflater.inflate(R.layout.list_item_icon_text, null);
            convertView = inflater.inflate(R.layout.list_item_icon_text, parent, false);
            holder = new ViewHolder();
            
            holder.iv = (ImageView) convertView.findViewById(R.id.thumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.list_item_progressbar);
            holder.progressTxt = (TextView) convertView.findViewById(R.id.list_item_progresstxt);
            
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
            
        FileInfo f = fileItems.get(position);
        int progress = pref.getInt(f.getId(), 0);
        int oldPosition = holder.position;
        holder.position = position;
        holder.title.setText(f.getName());
        holder.progressBar.setMax(f.getList().size()-1);
        holder.progressBar.setProgress(progress);
        holder.progressTxt.setText((progress+1) + "/" + (f.getList().size()));
        
        if (position % 2 == 1) {
            convertView.setBackgroundResource(R.drawable.list_row_selector_odd);
        } else {
            convertView.setBackgroundResource(R.drawable.list_row_selector);
        }
        Log.d("ThumtailAdapter_getView", String.format("old to current:%d --> %d ",  oldPosition,position));
        new ThumbnailLoader(position, holder).execute(f.getId());
        return convertView;
    }
    
    private static class ViewHolder {
        public int position;
        public ImageView iv;
        public TextView title;
        public ProgressBar progressBar;
        public TextView progressTxt;
    }

}

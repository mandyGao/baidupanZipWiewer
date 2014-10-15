package com.jdapp.ddddddd.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.R;
import com.jdapp.ddddddd.activity.CustomThumtailTextAdapter;
import com.jdapp.ddddddd.activity.ImageViewActivity;
import com.jdapp.ddddddd.db.DBHelper;
import com.jdapp.ddddddd.model.FileInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class MyListFragment extends ListFragment {

    // private MyArrayAdapter mArrayAdapter;
    private CustomThumtailTextAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        Log.d("MyListFragment", "onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("MyListFragment", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("MyListFragment", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        adapter = new CustomThumtailTextAdapter(getActivity(), getData());
        setListAdapter(adapter);
        setEmptyText(getString(R.string.list_fragment_emptytext));
        getListView().setSelector(R.drawable.list_row_selector);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        // go to ZIP content
        ImageView iv = (ImageView) v.findViewById(R.id.thumbnail);
        Object tag = iv.getTag(R.id.TAG_NEED_THUMB);
        boolean needThumb = true;
        if (tag != null && (Boolean) tag == false) {
            needThumb = false;
        }
        FileInfo f = (FileInfo) adapter.getItem(position);
        Intent intent = new Intent(getActivity(), ImageViewActivity.class);
        intent.putExtra(App.bundleKeyFileinfo, f);
        intent.putExtra("NEED_THUMB", needThumb);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("MyListFragment", "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPause() {
        adapter.getThumbAvalibledict().clear();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void setEmptyText(CharSequence text) {
        super.setEmptyText(text);
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        super.setListAdapter(adapter);
    }

    private ArrayList<FileInfo> getData() {
        DBHelper dbh = new DBHelper(getActivity());
        String dataString = dbh.queryForData();
        return fileInfoFromJson(dataString);
    }

    public static ArrayList<FileInfo> fileInfoFromJson(String json) {
        ArrayList<FileInfo> dataSet = new ArrayList<FileInfo>();
        try {
            JSONArray ja = new JSONArray(json);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject job = ja.getJSONObject(i);
                String path = job.getString("path");
                int total = job.getInt("total");
                JSONArray fileNameJa = job.getJSONArray("list");
                ArrayList<String> list = new ArrayList<String>();
                for (int j = 0; j < fileNameJa.length(); j++) {
                    list.add(fileNameJa.getString(j));
                }
                String id = job.getString("id");
                String name = job.getString("name");
                FileInfo f = new FileInfo(path, total, list, id, name);
                dataSet.add(f);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataSet;
    }

}

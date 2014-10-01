package com.jdapp.ddddddd.ui;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.activity.ImageViewActivity;
import com.jdapp.ddddddd.activity.MyArrayAdapter;
import com.jdapp.ddddddd.db.DBHelper;
import com.jdapp.ddddddd.model.FileInfo;
import com.jdapp.ddddddd.utils.Http;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyListFragment extends ListFragment {

    private MyArrayAdapter mArrayAdapter;
    private ArrayList<FileInfo> infoSet;
    private FileInfo updatebtn;
    
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
        infoSet = getData();
        mArrayAdapter = new MyArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
        updatebtn = new FileInfo("path", 0, new ArrayList<String>(), "0",
                "UPDATE");
        mArrayAdapter.add(updatebtn);
        mArrayAdapter.addAllforApi10(infoSet);
        setListAdapter(mArrayAdapter);
        setEmptyText("no data now :(");
        setListShown(true);
    }
    

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (0 == position) {
            if (null == App.sessionApi) {
                Toast.makeText(getActivity(), "read property file failed,check it",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Http.setCookie(App.sessionApi);
            Http.get("http://ddddddd.jd-app.com/comic/api/", null,
                    new AsyncHttpResponseHandler() {

                        @Override
                        @Deprecated
                        public void onSuccess(int statusCode, Header[] headers,
                                String content) {
                            Log.d("onSuccess", content);
                            mArrayAdapter.clear();
                            mArrayAdapter.add(updatebtn);
                            mArrayAdapter.addAllforApi10(fileInfoFromJson(content));
                            mArrayAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(),
                                    "update success. ", Toast.LENGTH_LONG)
                                    .show();

                            DBHelper dbh = new DBHelper(getActivity());
                            dbh.addOrUpdate(content);
                        }

                        @Override
                        @Deprecated
                        public void onFailure(int statusCode, Header[] headers,
                                Throwable error, String content) {
                            Log.d("onfailure", "" + statusCode + content);
                            Toast.makeText(
                                    getActivity(),
                                    "update failed: check network or is cookie outdate?",
                                    Toast.LENGTH_LONG).show();
                        }

                    });

        } else {
            // go to ZIP content
            FileInfo f = mArrayAdapter.getItem(position);
            Intent intent = new Intent(getActivity(), ImageViewActivity.class);
            intent.putExtra(App.bundleKeyFileinfo, f);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("MyListFragment", "onCreateView");  
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setEmptyText(CharSequence text) {
        // TODO Auto-generated method stub
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
    
    private ArrayList<FileInfo> fileInfoFromJson(String json) {
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

package com.jdapp.ddddddd.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jdapp.ddddddd.App;
import com.jdapp.ddddddd.db.DBHelper;
import com.jdapp.ddddddd.model.FileInfo;
import com.jdapp.ddddddd.utils.Http;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private MyArrayAdapter mAdapter;
	private ArrayList<FileInfo> infoSet;
	private FileInfo updatebtn;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		infoSet = getData();
		mAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1);
		updatebtn = new FileInfo("path", 0, new ArrayList<String>(), "0", "UPDATE");
		mAdapter.add(updatebtn);
		mAdapter.addAllforApi10(infoSet);
		setListAdapter(mAdapter);
		LoadSessions();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (0 == position) {
			if (null == App.sessionApi) {
				Toast.makeText(this, "read property file failed,check it", Toast.LENGTH_LONG).show();
				return;
			}
			Http.setCookie(App.sessionApi);
			Http.get("http://ddddddd.jd-app.com/comic/api/", null,new AsyncHttpResponseHandler(){

				@Override
				@Deprecated
				public void onSuccess(int statusCode, Header[] headers,
						String content) {
					Log.d("onSuccess", content);
					mAdapter.clear();
					mAdapter.add(updatebtn);
					mAdapter.addAllforApi10(fileInfoFromJson(content));
					mAdapter.notifyDataSetChanged();
					Toast.makeText(MainActivity.this, "update success. " , Toast.LENGTH_LONG)
					.show();
					
					DBHelper dbh = new DBHelper(MainActivity.this);
					dbh.addOrUpdate(content);
				}

				@Override
				@Deprecated
				public void onFailure(int statusCode, Header[] headers,
						Throwable error, String content) {
					Log.d("onfailure", ""+statusCode+content);
					Toast.makeText(MainActivity.this, "update failed: is cookie outdate?", Toast.LENGTH_LONG)
					.show();
				}
				
				
			});
			
		} else {
			// go to ZIP content
			FileInfo f = mAdapter.getItem(position);
			Intent intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra(App.bundleKeyFileinfo, f);
			startActivity(intent);
		}

	}
	
	
	private ArrayList<FileInfo> getData() {
		DBHelper dbh = new DBHelper(this);
		String dataString = dbh.queryForData();
		return fileInfoFromJson(dataString);
	}
	
	private ArrayList<FileInfo> fileInfoFromJson(String json){
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
	
	private void LoadSessions(){
		Properties properties = new Properties();
		File file = new File(App.APP_EXTERNAL_FILES_DIR,"session.properties");
		try {
			FileInputStream fis = new FileInputStream(file);
			properties.load(fis);
			fis.close();
			App.sessionApi = properties.getProperty("api");
			App.sessionBaiduPan = properties.getProperty("baidu");
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "read property file failed,file not eixts:"+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, "read property file failed,read err:"+file.getAbsolutePath(), Toast.LENGTH_LONG).show();
		}
	}

}

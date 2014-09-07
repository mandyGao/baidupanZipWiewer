package com.jdapp.ddddddd.activity;

import java.util.Collection;

import com.jdapp.ddddddd.model.FileInfo;

import android.content.Context;
import android.widget.ArrayAdapter;

public class MyArrayAdapter extends ArrayAdapter<FileInfo> {

	public MyArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	
	public void addAllforApi10(Collection<FileInfo> collection) {
		for (FileInfo i : collection) {
			this.add(i);
		}
	}
	

}

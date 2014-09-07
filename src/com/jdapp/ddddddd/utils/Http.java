package com.jdapp.ddddddd.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class Http {
	
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.get(url, params, responseHandler);
	  }
	
	public static void get(Context context, String url, AsyncHttpResponseHandler responseHandler){
		client.get(context, url, responseHandler);
	}
	
	public static void setCookie(String cookiestr) {
	      client.addHeader("Cookie", cookiestr);
	  }
	
	public static void cancelAll(Context context){
		client.cancelRequests(context, true);
	}

}

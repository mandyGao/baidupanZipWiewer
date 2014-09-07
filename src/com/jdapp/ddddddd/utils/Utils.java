package com.jdapp.ddddddd.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import android.net.Uri;

import com.jdapp.ddddddd.model.FileInfo;

public class Utils {
	public static String md5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Huh, UTF-8 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}
	
	public static ArrayList<String> getUrls(FileInfo f){
		String BAIDU_FILE_PRE = "http://pcs.baidu.com/rest/2.0/pcs/file?method=unzipdownload&app_id=250528";
		ArrayList<String> urls = new ArrayList<String>();
		ArrayList<String> fileNames = f.getList();
		String path = f.getPath();
		String fileNameTem;
		String urlTem;
		Iterator<String> it = fileNames.iterator();
		while(it.hasNext()){
			fileNameTem = it.next();
			urlTem = BAIDU_FILE_PRE+"&path="+Uri.encode(path)+
					"&subpath="+Uri.encode(fileNameTem);
			urls.add(urlTem);
		}
		return urls;
	}
	
}

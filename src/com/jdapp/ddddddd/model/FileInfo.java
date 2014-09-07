package com.jdapp.ddddddd.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Owner
 * a class to describe a zipfile's information
 *  in baidu pan.
 */
public class FileInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String path;
	private int total;
	private ArrayList<String> list;
	private String id;
	private String name;
	
	public FileInfo( ) {
	}
	
	public FileInfo(String path, int total, ArrayList<String> list, String id, String name) {
		super();
		this.path = path;
		this.total = total;
		this.list = list;
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public ArrayList<String> getList() {
		return list;
	}

	public void setList(ArrayList<String> list) {
		this.list = list;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
	
	

}

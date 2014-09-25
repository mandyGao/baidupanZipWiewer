package com.jdapp.ddddddd.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Owner a class to describe a zipfile's information in baidu pan.
 */
public class FileInfo implements Parcelable {
    /**
	 * 
	 */
    private String path;
    private int total;
    private ArrayList<String> list;
    private String id;
    private String name;

    public FileInfo() {
    }

    public FileInfo(String path, int total, ArrayList<String> list, String id,
            String name) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * private String path;
     * private int total;
     * private ArrayList<String> list;
     * private String id;
     * private String name;
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeInt(total);
        dest.writeStringList(list);
        dest.writeString(id);
        dest.writeString(name);

    }

    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    public FileInfo(Parcel in) {
        this.list = new ArrayList<String>();
        this.path = in.readString();
        this.total = in.readInt();
        in.readStringList(this.list);
        this.id = in.readString();
        this.name = in.readString();
    }

}

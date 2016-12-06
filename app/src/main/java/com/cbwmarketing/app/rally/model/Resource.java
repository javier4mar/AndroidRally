package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Juan-Crawford on 11/11/2016.
 */

public class Resource implements Parcelable {
    private long createddate;
    private String title;
    private String type;
    private String url;

    public long getCreateddate() {
        return createddate;
    }

    public void setCreateddate(long createddate) {
        this.createddate = createddate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Resource(){}

    public Resource(Parcel in) {
        this();
        this.createddate = in.readLong();
        this.title = in.readString();
        this.type = in.readString();
        this.url = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.createddate);
        out.writeString(this.title);
        out.writeString(this.type);
        out.writeString(this.url);
    }

    public static final Creator<Resource> CREATOR = new Creator<Resource>() {
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };
}

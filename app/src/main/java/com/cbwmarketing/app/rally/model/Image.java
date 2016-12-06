package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jcrawford on 8/11/2016.
 */

public class Image implements Parcelable {
    private String name;
    private int status;
    private String title;
    private String type;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public Image() {
    }

    public Image(Parcel in) {
        this();
        this.name = in.readString();
        this.status = in.readInt();
        this.title = in.readString();
        this.type = in.readString();
        this.url = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeInt(this.status);
        out.writeString(this.title);
        out.writeString(this.type);
        out.writeString(this.url);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}

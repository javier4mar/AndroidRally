package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.CircleOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcrawford on 8/11/2016.
 */

public class Challenge implements Parcelable {
    private String challengeId;
    private String body;
    private long createddate;
    private long gameid;
    private List<Image> images;
    private double latitude;
    private double longitude;
    private int status;
    private String title;
    private int visible;
    private CircleOptions radius;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getCreateddate() {
        return createddate;
    }

    public void setCreateddate(long createddate) {
        this.createddate = createddate;
    }

    public long getGameid() {
        return gameid;
    }

    public void setGameid(long gameid) {
        this.gameid = gameid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public CircleOptions getRadius() {
        return radius;
    }

    public void setRadius(CircleOptions radius) {
        this.radius = radius;
    }

    public Challenge() {
        this.images = new ArrayList<>();
    }

    public Challenge(Parcel in) {
        this();
        this.challengeId = in.readString();
        this.body = in.readString();
        this.createddate = in.readLong();
        this.gameid = in.readLong();
        in.readList(this.images, Image.class.getClassLoader());
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.status = in.readInt();
        this.title = in.readString();
        this.visible = in.readInt();
        this.radius = in.readParcelable(CircleOptions.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.challengeId);
        out.writeString(this.body);
        out.writeLong(this.createddate);
        out.writeLong(this.gameid);
        out.writeList(this.images);
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
        out.writeInt(this.status);
        out.writeString(this.title);
        out.writeInt(this.visible);
        out.writeParcelable(this.radius, flags);
    }

    public static final Creator<Challenge> CREATOR = new Creator<Challenge>() {
        public Challenge createFromParcel(Parcel in) {
            return new Challenge(in);
        }

        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };
}

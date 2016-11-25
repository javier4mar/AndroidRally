package com.easywaypop.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class HelpRequest implements Parcelable {
    private String requestId;
    private long createddate;
    private String firebaseuid;
    private long gameid;
    private double latitude;
    private double longitude;
    private int status;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getCreateddate() {
        return createddate;
    }

    public void setCreateddate(long createddate) {
        this.createddate = createddate;
    }

    public String getFirebaseuid() {
        return firebaseuid;
    }

    public void setFirebaseuid(String firebaseuid) {
        this.firebaseuid = firebaseuid;
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

    public HelpRequest() {
    }

    public HelpRequest(Parcel in) {
        this();
        this.requestId = in.readString();
        this.createddate = in.readLong();
        this.firebaseuid = in.readString();
        this.gameid = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.status = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.requestId);
        out.writeLong(this.createddate);
        out.writeString(this.firebaseuid);
        out.writeLong(this.gameid);
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
        out.writeInt(this.status);
    }

    public static final Creator<HelpRequest> CREATOR = new Creator<HelpRequest>() {
        public HelpRequest createFromParcel(Parcel in) {
            return new HelpRequest(in);
        }

        public HelpRequest[] newArray(int size) {
            return new HelpRequest[size];
        }
    };
}

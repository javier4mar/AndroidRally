package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Juan-Crawford on 9/11/2016.
 */

public class DoneChallenge implements Parcelable {
    private long challengeid;
    private long datefinish;
    private long datestarted;
    private long gameid;
    private double latitude;
    private double longitude;
    private Resource resources;
    private int status;
    private long teamid;

    public long getChallengeid() {
        return challengeid;
    }

    public void setChallengeid(long challengeid) {
        this.challengeid = challengeid;
    }

    public long getDatefinish() {
        return datefinish;
    }

    public void setDatefinish(long datefinish) {
        this.datefinish = datefinish;
    }

    public long getDatestarted() {
        return datestarted;
    }

    public void setDatestarted(long datestarted) {
        this.datestarted = datestarted;
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

    public Resource getResources() {
        return resources;
    }

    public void setResources(Resource resources) {
        this.resources = resources;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTeamid() {
        return teamid;
    }

    public void setTeamid(long teamid) {
        this.teamid = teamid;
    }

    public DoneChallenge(){}

    public DoneChallenge(Parcel in) {
        this();
        this.challengeid = in.readLong();
        this.datefinish = in.readLong();
        this.datestarted = in.readLong();
        this.gameid = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.resources = in.readParcelable(Resource.class.getClassLoader());
        this.status = in.readInt();
        this.teamid = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.challengeid);
        out.writeLong(this.datefinish);
        out.writeLong(this.datestarted);
        out.writeLong(this.gameid);
        out.writeDouble(this.latitude);
        out.writeDouble(this.longitude);
        out.writeParcelable(this.resources, flags);
        out.writeInt(this.status);
        out.writeLong(this.teamid);
    }

    public static final Creator<DoneChallenge> CREATOR = new Creator<DoneChallenge>() {
        public DoneChallenge createFromParcel(Parcel in) {
            return new DoneChallenge(in);
        }

        public DoneChallenge[] newArray(int size) {
            return new DoneChallenge[size];
        }
    };
}

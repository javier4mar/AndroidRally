package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jcrawford on 9/11/2016.
 */

public class Team implements Parcelable{
    private long gameid;
    private String name;
    private int status;
    private long teamid;
    private String teamimage;

    public long getGameid() {
        return gameid;
    }

    public void setGameid(long gameid) {
        this.gameid = gameid;
    }

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

    public long getTeamid() {
        return teamid;
    }

    public void setTeamid(long teamid) {
        this.teamid = teamid;
    }

    public String getTeamimage() {
        return teamimage;
    }

    public void setTeamimage(String teamimage) {
        this.teamimage = teamimage;
    }

    public Team(){}

    public Team(Parcel in) {
        this();
        this.gameid = in.readLong();
        this.name = in.readString();
        this.status = in.readInt();
        this.teamid = in.readLong();
        this.teamimage = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.gameid);
        out.writeString(this.name);
        out.writeInt(this.status);
        out.writeLong(this.teamid);
        out.writeString(this.teamimage);
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        public Team[] newArray(int size) {
            return new Team[size];
        }
    };
}

package com.easywaypop.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jcrawford on 7/11/2016.
 */

public class User implements Parcelable{
    private String firebaseuid;
    private long gameid;
    private String lastname;
    private String name;
    private String rol;
    private long teamid;

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

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public long getTeamid() {
        return teamid;
    }

    public void setTeamid(long teamid) {
        this.teamid = teamid;
    }

    public User(){}

    public User(Parcel in) {
        this();
        this.firebaseuid = in.readString();
        this.gameid = in.readLong();
        this.lastname = in.readString();
        this.name = in.readString();
        this.rol = in.readString();
        this.teamid = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.firebaseuid);
        out.writeLong(this.gameid);
        out.writeString(this.lastname);
        out.writeString(this.name);
        out.writeString(this.rol);
        out.writeLong(this.teamid);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}

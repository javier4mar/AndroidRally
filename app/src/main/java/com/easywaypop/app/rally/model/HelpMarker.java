package com.easywaypop.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Juan-Crawford on 13/11/2016.
 */

public class HelpMarker implements Parcelable {
    private User user;
    private Team team;
    private HelpRequest helpRequest;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public HelpRequest getHelpRequest() {
        return helpRequest;
    }

    public void setHelpRequest(HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    public HelpMarker() {
    }

    public HelpMarker(Parcel in) {
        this();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.team = in.readParcelable(Team.class.getClassLoader());
        this.helpRequest = in.readParcelable(HelpRequest.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.user, flags);
        out.writeParcelable(this.team, flags);
        out.writeParcelable(this.helpRequest, flags);
    }

    public static final Creator<HelpMarker> CREATOR = new Creator<HelpMarker>() {
        public HelpMarker createFromParcel(Parcel in) {
            return new HelpMarker(in);
        }

        public HelpMarker[] newArray(int size) {
            return new HelpMarker[size];
        }
    };
}

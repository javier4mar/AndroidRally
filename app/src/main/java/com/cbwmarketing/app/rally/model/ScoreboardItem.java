package com.cbwmarketing.app.rally.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class ScoreboardItem implements Parcelable {
    private Team team;
    private List<Challenge> challengeList;
    private List<DoneChallenge> doneChallengeList;
    private List<User> userChallengeList;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
        setChallengeList(new ArrayList<Challenge>());
        setDoneChallengeList(new ArrayList<DoneChallenge>());
        setUserList(new ArrayList<User>());

    }

    public List<Challenge> getChallengeList() {
        return challengeList;
    }

    private void setChallengeList(List<Challenge> challengeList) {
        this.challengeList = challengeList;
    }

    public List<User> getUserList() {
        return userChallengeList;
    }

    private void setUserList(List<User> mUserChallengeList) {
        this.userChallengeList = mUserChallengeList;
    }

    public List<DoneChallenge> getDoneChallengeList() {
        return doneChallengeList;
    }

    private void setDoneChallengeList(List<DoneChallenge> doneChallengeList) {
        this.doneChallengeList = doneChallengeList;
    }

    public ScoreboardItem() {
        this.challengeList = new ArrayList<>();
        this.doneChallengeList = new ArrayList<>();
        this.userChallengeList = new ArrayList<>();

    }

    public ScoreboardItem(Parcel in) {
        this();
        this.team = in.readParcelable(Team.class.getClassLoader());
        in.readList(this.challengeList, Challenge.class.getClassLoader());
        in.readList(this.doneChallengeList, DoneChallenge.class.getClassLoader());
        in.readList(this.userChallengeList, User.class.getClassLoader());

    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.team, flags);
        out.writeList(this.challengeList);
        out.writeList(this.doneChallengeList);
        out.writeList(this.userChallengeList);

    }

    public static final Creator<ScoreboardItem> CREATOR = new Creator<ScoreboardItem>() {
        public ScoreboardItem createFromParcel(Parcel in) {
            return new ScoreboardItem(in);
        }

        public ScoreboardItem[] newArray(int size) {
            return new ScoreboardItem[size];
        }
    };
}

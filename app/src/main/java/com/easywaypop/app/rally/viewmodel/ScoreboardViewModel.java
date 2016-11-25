package com.easywaypop.app.rally.viewmodel;

import android.content.Context;
import android.databinding.ObservableInt;
import android.view.View;

import com.easywaypop.app.rally.Rally;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.model.Resource;
import com.easywaypop.app.rally.model.ScoreboardItem;
import com.easywaypop.app.rally.model.Team;
import com.easywaypop.app.rally.utility.CustomComparator;
import com.easywaypop.app.rally.view.BaseActivity;
import com.easywaypop.app.rally.view.ScoreboardFragment;
import com.easywaypop.app.rally.view.TeamActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jcrawford on 10/11/2016.
 */

public class ScoreboardViewModel implements MainViewModelContract.ViewModel{

    public ObservableInt infoMessageVisibility;
    public ObservableInt progressVisibility;
    public ObservableInt recyclerViewVisibility;

    private Context mContext;
    private ScoreboardFragment mFragment;
    private List<ScoreboardItem> mScoreboardList;

    public ScoreboardViewModel(MainViewModelContract.MainView mainView, Context context) {
        this.mContext = context;
        this.mFragment = (ScoreboardFragment) mainView;
        this.progressVisibility = new ObservableInt(View.INVISIBLE);
        this.infoMessageVisibility = new ObservableInt(View.INVISIBLE);
        this.recyclerViewVisibility = new ObservableInt(View.INVISIBLE);
        this.getImage();
    }

    private void showProgress(){
        this.progressVisibility.set(View.VISIBLE);
        this.recyclerViewVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.GONE);
    }

    private void showError(){
        this.progressVisibility.set(View.GONE);
        this.recyclerViewVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.VISIBLE);
    }

    private void hideProgress(){
        this.progressVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.GONE);
        this.recyclerViewVisibility.set(View.VISIBLE);
    }

    private void getImage(){
        getActivity().getCompanyImage(new BaseActivity.OnLoadCompanyImageListener() {
            @Override
            public void onLoadImage(List<Resource> resources) {
                for (Resource item : resources) {
                    if (item != null && item.getTitle().equalsIgnoreCase(BaseActivity.LOGIN_IMAGE)) {
                        mFragment.loadImage(item.getUrl());
                        break;
                    }
                }
            }
        });
    }

    private void registerTeamsListener() {
        showProgress();
        getActivity().mDatabase.child("teams").addValueEventListener(teamsListener);
    }

    private void unRegisterTeamsListener() {
        getActivity().mDatabase.child("teams").removeEventListener(teamsListener);
    }

    private ValueEventListener teamsListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mScoreboardList = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final Team team = postSnapshot.getValue(Team.class);
                if(team.getGameid() == getActivity().mPreferencesManager.getGameId()
                        && team.getTeamid() != 0){
                    ScoreboardItem item = new ScoreboardItem();
                    item.setTeam(team);
                    mScoreboardList.add(item);
                }
            }
            registerChallengesListener();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            showError();
            unRegisterTeamsListener();
        }
    };

    private void registerChallengesListener() {
        showProgress();
        getActivity().mDatabase.child("challenges").addValueEventListener(challengesListener);
    }

    private void unRegisterChallengesListener() {
        getActivity().mDatabase.child("challenges").removeEventListener(challengesListener);
    }

    private ValueEventListener challengesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final Challenge challenge = postSnapshot.getValue(Challenge.class);
                if (challenge.getGameid() == getActivity().mPreferencesManager.getGameId()
                        && challenge.getVisible() == BaseActivity.STATUS_ACTIVE) {
                    challenge.setChallengeId(postSnapshot.getKey());
                    addChallenge(challenge);
                }
            }
            unRegisterChallengesListener();
            registerDoneChallengesListener();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            showError();
            unRegisterChallengesListener();
        }
    };

    private void registerDoneChallengesListener() {
        showProgress();
        getActivity().mDatabase.child("donechallenges").addValueEventListener(doneChallengesListener);
    }

    private void unRegisterDoneChallengesListener() {
        getActivity().mDatabase.child("donechallenges").removeEventListener(doneChallengesListener);
    }

    private ValueEventListener doneChallengesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            hideProgress();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final DoneChallenge doneChallenge = postSnapshot.getValue(DoneChallenge.class);
                if(doneChallenge.getGameid() == getActivity().mPreferencesManager.getGameId())
                    addDoneChallenge(doneChallenge);
            }
            Collections.sort(mScoreboardList, new CustomComparator());
            mFragment.loadScoreboard(mScoreboardList);
            unRegisterDoneChallengesListener();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            showError();
            unRegisterDoneChallengesListener();
        }
    };

    private void addChallenge(Challenge challenge) {
        if (mScoreboardList != null) {
            for (ScoreboardItem item : mScoreboardList) {
                item.getChallengeList().add(challenge);
            }
        }
    }

    private void addDoneChallenge(DoneChallenge doneChallenge) {
        if (mScoreboardList != null) {
            for (int i = 0; i < mScoreboardList.size(); i++) {
                if (mScoreboardList.get(i).getTeam().getTeamid() == doneChallenge.getTeamid()) {
                    mScoreboardList.get(i).getDoneChallengeList().add(doneChallenge);
                    break;
                }
            }
        }
    }

    public void onClick(View view){
        registerTeamsListener();
    }

    private TeamActivity getActivity() {
        return ((TeamActivity) mContext);
    }


    @Override
    public void onResume() {
        registerTeamsListener();
    }

    @Override
    public void onPause() {
        unRegisterTeamsListener();
        unRegisterChallengesListener();
        unRegisterDoneChallengesListener();
    }

    @Override
    public void destroy() {
        unRegisterTeamsListener();
        unRegisterChallengesListener();
        unRegisterDoneChallengesListener();
        Rally.getInstance().setTeamWinner(null);
        mContext = null;
    }
}

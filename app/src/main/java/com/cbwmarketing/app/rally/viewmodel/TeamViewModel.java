package com.cbwmarketing.app.rally.viewmodel;

import android.content.Context;
import android.databinding.ObservableInt;
import android.view.View;

import com.cbwmarketing.app.rally.model.Challenge;
import com.cbwmarketing.app.rally.model.DoneChallenge;
import com.cbwmarketing.app.rally.model.ScoreboardItem;
import com.cbwmarketing.app.rally.model.Team;
import com.cbwmarketing.app.rally.model.User;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.cbwmarketing.app.rally.view.TeamActivity;
import com.cbwmarketing.app.rally.view.TeamFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class TeamViewModel implements MainViewModelContract.ViewModel {

    public ObservableInt infoMessageVisibility;
    public ObservableInt progressVisibility;
    public ObservableInt recyclerViewVisibility;

    private Context mContext;
    private TeamFragment mFragment;
    private ScoreboardItem mScoreboardItem;
    private long mTeamId;

    public TeamViewModel(MainViewModelContract.MainView mainView, Context context, long teamId) {
        this.mContext = context;
        this.mFragment = (TeamFragment) mainView;
        this.mTeamId = teamId;
        this.progressVisibility = new ObservableInt(View.INVISIBLE);
        this.infoMessageVisibility = new ObservableInt(View.INVISIBLE);
        this.recyclerViewVisibility = new ObservableInt(View.INVISIBLE);
    }

    private void showProgress() {
        this.progressVisibility.set(View.VISIBLE);
        this.recyclerViewVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.GONE);
    }

    private void showError() {
        this.progressVisibility.set(View.GONE);
        this.recyclerViewVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.VISIBLE);
    }

    public void hideProgress() {
        this.progressVisibility.set(View.GONE);
        this.infoMessageVisibility.set(View.GONE);
        this.recyclerViewVisibility.set(View.VISIBLE);
    }

    private void loadTeamData() {
        registerTeamsListener();
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
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final Team team = postSnapshot.getValue(Team.class);
                if (team.getGameid() == getActivity().mPreferencesManager.getGameId()
                        && team.getTeamid() == mTeamId) {
                    mScoreboardItem = new ScoreboardItem();
                    mScoreboardItem.setTeam(team);
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
                    mScoreboardItem.getChallengeList().add(challenge);
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
                if (doneChallenge.getGameid() == getActivity().mPreferencesManager.getGameId() &&
                        doneChallenge.getTeamid() == mTeamId)
                    mScoreboardItem.getDoneChallengeList().add(doneChallenge);
            }
            unRegisterDoneChallengesListener();
            mFragment.initViews(mScoreboardItem);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            showError();
            unRegisterDoneChallengesListener();
        }
    };

    public void onClick(View view) {
        loadTeamData();
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
        mContext = null;
        mFragment = null;
    }
}
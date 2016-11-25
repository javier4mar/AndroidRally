package com.easywaypop.app.rally.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.Rally;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.model.Game;
import com.easywaypop.app.rally.model.ScoreboardItem;
import com.easywaypop.app.rally.model.Team;
import com.easywaypop.app.rally.service.ExtraChallengeService;
import com.easywaypop.app.rally.service.GPSTracker;
import com.easywaypop.app.rally.service.HelpRequestService;
import com.easywaypop.app.rally.utility.CustomComparator;
import com.easywaypop.app.rally.view.BaseActivity;
import com.easywaypop.app.rally.view.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Juan-Crawford on 8/11/2016.
 */

public class MainViewModel implements MainViewModelContract.ViewModel, ValueEventListener {

    private Context mContext;
    private List<ScoreboardItem> mScoreboardList;
    private GPSTracker mGps;

    public MainViewModel(Context context) {
        this.mContext = context;
        if (!getActivity().isAdmin()) startExtraChallengeService();
        else startHelpRequestService();
    }

    private void startHelpRequestService() {
        Intent intent = new Intent(HelpRequestService.SERVICE_ACTION);
        intent.setPackage(BaseActivity.PACKAGE_NAME);
        mContext.startService(intent);
    }

    private void stopHelpRequestService() {
        Intent intent = new Intent(HelpRequestService.SERVICE_ACTION);
        intent.setPackage(BaseActivity.PACKAGE_NAME);
        mContext.stopService(intent);
    }

    private void startExtraChallengeService() {
        Intent intent = new Intent(ExtraChallengeService.SERVICE_ACTION);
        intent.setPackage(BaseActivity.PACKAGE_NAME);
        mContext.startService(intent);
    }

    private void stopExtraChallengeService() {
        Intent intent = new Intent(ExtraChallengeService.SERVICE_ACTION);
        intent.setPackage(BaseActivity.PACKAGE_NAME);
        mContext.stopService(intent);
    }

    public void getTeamInfo() {
        getActivity().mDatabase.child("teams").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Team team = postSnapshot.getValue(Team.class);
                    if (team.getTeamid() == getActivity().mPreferencesManager.getTeamId() &&
                            team.getGameid() == getActivity().mPreferencesManager.getGameId()) {
                        if (mContext != null) getActivity().setInfoInDrawer(team);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void generateDialog(String msg, DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg)
                .setTitle(R.string.app_name)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        builder.create().show();
    }

    public void confirmActiveBonusChallenge() {
        final String msg = mContext.getString(R.string.confirm_bonus_challenge_msg);
        generateDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activeBonusChallenge();
            }
        });
    }

    private void activeBonusChallenge() {
        getActivity().mDatabase.child("challenges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Challenge challenge = postSnapshot.getValue(Challenge.class);
                    if (challenge.getGameid() == getActivity().mPreferencesManager.getGameId()
                            && challenge.getVisible() == 0 && challenge.getStatus() == 2) {
                        getActivity().mDatabase.child("challenges").child(postSnapshot.getKey())
                                .child("visible").setValue(1);
                        getActivity().showToastMessage(mContext.getString(R.string.bonus_challenge_activated_successfully));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void confirmHelpRequest() {
        final String msg = mContext.getString(R.string.call_admin_confirmation_msg);
        generateDialog(msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setHelpRequest();
            }
        });
    }

    private void setHelpRequest() {
        mGps = new GPSTracker(mContext);
        if (mGps.canGetLocation()) {
            HashMap<String, Object> _result = new HashMap<>();
            _result.put("createddate", System.currentTimeMillis());
            _result.put("firebaseuid", getActivity().mFirebaseUser.getUid());
            _result.put("gameid", getActivity().mPreferencesManager.getGameId());
            _result.put("latitude", mGps.getLatitude());
            _result.put("longitude", mGps.getLongitude());
            _result.put("status", BaseActivity.STATUS_ACTIVE);
            getActivity().mDatabase.child("helprequest").push().setValue(_result);
            getActivity().showToastMessage(mContext.getString(R.string.moderator_call_created_msg));
        } else
            getActivity().showGeneralAlertDialog(mContext.getString(R.string.need_gps_error_msg));
    }

    private void registerGameListener() {
        getActivity().mDatabase.child("games").addValueEventListener(this);
    }

    private void unRegisterGameListener() {
        getActivity().mDatabase.child("games").removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            final Game game = postSnapshot.getValue(Game.class);
            if (game.getGameid() == getActivity().mPreferencesManager.getGameId()) {
                switch (game.getStatus()) {
                    case BaseActivity.STATUS_ACTIVE:
                        getActivity().initChronometer(0);
                        break;
                    case BaseActivity.STATUS_RUNNING:
                        if (System.currentTimeMillis() < game.getFinishdate())
                            getActivity().initChronometer(getActivity().getCurrentTime(game));
                        else {
                            getActivity().initChronometer(0);
                            finishGameByTime();
                        }
                        break;
                    case BaseActivity.STATUS_FINISHED:
                        getActivity().initChronometer(0);
                        if (Rally.getInstance().getTeamWinner() != null) showWinnerDialog();
                        else getWinnerByTimeOff();
                        break;
                }
                break;
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    public void finishGameByTime() {
        getActivity().mDatabase.child("games").child(String.valueOf(getActivity().
                mPreferencesManager.getGameId())).child("status").setValue(BaseActivity.STATUS_FINISHED);
    }

    private void getWinnerByTimeOff() {
        getActivity().mDatabase.child("teams").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mScoreboardList = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Team team = postSnapshot.getValue(Team.class);
                    if (team.getGameid() == getActivity().mPreferencesManager.getGameId()
                            && team.getTeamid() != 0) {
                        ScoreboardItem item = new ScoreboardItem();
                        item.setTeam(team);
                        mScoreboardList.add(item);
                    }
                    getActivity().mDatabase.child("teams").removeEventListener(this);
                }
                getActivity().mDatabase.child("donechallenges").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            final DoneChallenge doneChallenge = postSnapshot.getValue(DoneChallenge.class);
                            if (doneChallenge.getGameid() == getActivity().mPreferencesManager.getGameId())
                                addDoneChallenge(doneChallenge);
                        }
                        Collections.sort(mScoreboardList, new CustomComparator());
                        if (mScoreboardList.size() > 0) {
                            Rally.getInstance().setTeamWinner(mScoreboardList.get(0).getTeam());
                            showWinnerDialog();
                        }
                        getActivity().mDatabase.child("donechallenges").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        getActivity().mDatabase.child("donechallenges").removeEventListener(this);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getActivity().mDatabase.child("teams").removeEventListener(this);
            }
        });
        mContext.getString(R.string.time_off_finish_game_msg);
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

    private void showWinnerDialog() {
        getActivity().showGeneralAlertDialog(getCongratulationsMsg(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().mPreferencesManager.setIsGameStarted(false);
                        getActivity().mPreferencesManager.setIsExtraChallengeActivated(false);
                        ActivityCompat.finishAffinity(getActivity());
                    }
                });
    }

    private String getCongratulationsMsg() {
        return String.format(mContext.getString(R.string.congratulations_msg),
                Rally.getInstance().getTeamWinner().getName());
    }

    private MainActivity getActivity() {
        return ((MainActivity) mContext);
    }

    @Override
    public void onResume() {
        registerGameListener();
    }

    @Override
    public void onPause() {
        unRegisterGameListener();
    }

    @Override
    public void destroy() {
        if (getActivity().isAdmin()) stopHelpRequestService();
        else stopExtraChallengeService();
        unRegisterGameListener();
        if (mGps != null) mGps.stopUsingGPS();
        mContext = null;
    }
}

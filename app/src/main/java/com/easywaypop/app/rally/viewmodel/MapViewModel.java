package com.easywaypop.app.rally.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.Rally;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.model.HelpMarker;
import com.easywaypop.app.rally.service.GPSTracker;
import com.easywaypop.app.rally.service.UploadPhotoService;
import com.easywaypop.app.rally.view.BaseActivity;
import com.easywaypop.app.rally.view.MainActivity;
import com.easywaypop.app.rally.view.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcrawford on 3/11/2016.
 */

public class MapViewModel implements MainViewModelContract.ViewModel {

    private static final int DIALOG_DELAY = 250;
    private long mChallengeStarted;
    private Context mContext;
    private MapFragment mFragment;
    private GPSTracker mGps;
    private Challenge mSelectedChallenge;
    private List<Challenge> mChallengeList;
    private List<HelpMarker> mHelpMarkerList;

    public MapViewModel(MainViewModelContract.MainView mainView, Context context) {
        this.mContext = context;
        this.mFragment = (MapFragment) mainView.getFragment();
    }

    private void registerChallengesListener() {
        getActivity().mDatabase.child("challenges").addValueEventListener(challengesListener);
    }

    private void unRegisterChallengesListener() {
        getActivity().mDatabase.child("challenges").removeEventListener(challengesListener);
    }

    private ValueEventListener challengesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mChallengeList = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final Challenge challenge = postSnapshot.getValue(Challenge.class);
                if (challenge.getGameid() == getActivity().mPreferencesManager.getGameId()
                        && challenge.getVisible() == BaseActivity.STATUS_ACTIVE) {
                    challenge.setChallengeId(postSnapshot.getKey());
                    mChallengeList.add(challenge);
                }
            }
            if (getActivity().isAdmin()) {
                mFragment.setChallenges(mChallengeList);
                mFragment.setMarkersLoaded(false);
                mFragment.loadChallengesMarkers();
            } else
                registerDoneChallengesListener();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            unRegisterChallengesListener();
        }
    };

    private void registerDoneChallengesListener() {
        getActivity().mDatabase.child("donechallenges").addValueEventListener(doneChallengesListener);
    }

    private void unRegisterDoneChallengesListener() {
        getActivity().mDatabase.child("donechallenges").removeEventListener(doneChallengesListener);
    }

    private ValueEventListener doneChallengesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<DoneChallenge> doneChallengeList = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                final DoneChallenge doneChallenge = postSnapshot.getValue(DoneChallenge.class);
                if (doneChallenge.getTeamid() == getActivity().mPreferencesManager.getTeamId()) {
                    doneChallengeList.add(doneChallenge);
                    removeDoneChallenge(doneChallenge);
                }
            }
            mFragment.setChallenges(mChallengeList);
            mFragment.setDoneChallengeList(doneChallengeList);
            mFragment.setMarkersLoaded(false);
            mFragment.loadChallengesMarkers();
            unRegisterDoneChallengesListener();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            unRegisterDoneChallengesListener();
        }
    };

    private void removeDoneChallenge(DoneChallenge doneChallenge) {
        if (mChallengeList != null) {
            for (int i = 0; i < mChallengeList.size(); i++) {
                if (mChallengeList.get(i).getGameid() == doneChallenge.getGameid() && mChallengeList.get(i)
                        .getChallengeId().equals(String.valueOf(doneChallenge.getChallengeid()))) {
                    mChallengeList.remove(i);
                    break;
                }
            }
        }
    }

    public Challenge getChallenge(Marker marker){
        final double latitude = marker.getPosition().latitude;
        final double longitude = marker.getPosition().longitude;
        for (Challenge item : mFragment.getChallengeList()) {
            if (item.getLatitude() == latitude && item.getLongitude() == longitude)
                return item;
        }
        return null;
    }

    public boolean validateRadius(CircleOptions circle) {
        if(circle != null) {
            mGps = new GPSTracker(mContext);
            if (mGps.canGetLocation()) {
                float[] distance = new float[2];
                Location.distanceBetween(mGps.getLatitude(), mGps.getLongitude(),
                        circle.getCenter().latitude, circle.getCenter().longitude, distance);
                if (distance[0] > circle.getRadius()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().showGeneralAlertDialog(mContext
                                            .getString(R.string.cant_see_challenge_error_msg),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            getActivity().hideBottomSheet();
                                            mSelectedChallenge = null;
                                            dialogInterface.cancel();
                                        }
                                    });
                        }
                    }, DIALOG_DELAY);
                    return false;
                } else {
                    return true;
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFragment.showSettingsRequest();
                    }
                }, DIALOG_DELAY);
                return false;
            }
        }
        return false;
    }

    public void acceptChallenge(final Challenge challenge) {
        this.mSelectedChallenge = challenge;
        showBottomSheet(DIALOG_DELAY);
        final String msg = mContext.getString(R.string.accept_challenge_msg);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(BaseActivity.getMessage(msg))
                .setTitle(challenge.getTitle())
                .setCancelable(false)
                .setPositiveButton("GO!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().expandBottomSheet();
                                mChallengeStarted = System.currentTimeMillis();
                            }
                        }, 50);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().hideBottomSheet();
                        mSelectedChallenge = null;
                        dialogInterface.cancel();
                    }
                });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        }, DIALOG_DELAY);
    }

    public void showBottomSheet(long delay){
        mFragment.setInfoInBottomSheetWhenCollapsed();
        if (getActivity().isNeedCollapsed()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().showBottomSheetCollapsed();
                }
            }, delay);
        }
    }

    public void addHelpMarker(HelpMarker marker){
        if(mHelpMarkerList == null) mHelpMarkerList = new ArrayList<>();
        mHelpMarkerList.add(marker);
    }

    private HelpMarker getHelpRequest(Marker marker){
        final double latitude = marker.getPosition().latitude;
        final double longitude = marker.getPosition().longitude;
        for (HelpMarker item : mHelpMarkerList) {
            if (item.getHelpRequest().getLatitude() == latitude &&
                    item.getHelpRequest().getLongitude() == longitude)
                return item;
        }
        return null;
    }

    public void finishHelRequest(final Marker marker){
        final HelpMarker request = getHelpRequest(marker);
        if(request != null) {
            final String msg = String.format(mContext.getString(R.string.finish_help_confirmation_msg),
                    request.getUser().getName(), request.getUser().getLastname(), request.getTeam().getName());
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(BaseActivity.getMessage(msg))
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            marker.remove();
                            mHelpMarkerList = new ArrayList<>();
                            getActivity().mDatabase.child("helprequest").child(request.getHelpRequest()
                                    .getRequestId()).child("status").setValue(BaseActivity.STATUS_FINISHED);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    builder.create().show();
                }
            }, DIALOG_DELAY);
        }
    }

    public void uploadPhoto(Uri photoUri) {
        Intent uploadIntent = new Intent(UploadPhotoService.SERVICE_ACTION);
        uploadIntent.setPackage(BaseActivity.PACKAGE_NAME);
        uploadIntent.putExtra(UploadPhotoService.EXTRA_CHALLENGE_STARTED, mChallengeStarted);
        uploadIntent.putExtra(UploadPhotoService.EXTRA_CHALLENGE_SELECTED, mSelectedChallenge);
        uploadIntent.putExtra(UploadPhotoService.EXTRA_CHALLENGE_PHOTO, photoUri);
        mContext.startService(uploadIntent);
    }

    private MainActivity getActivity() {
        return ((MainActivity) mContext);
    }

    @Override
    public void onResume() {
        registerChallengesListener();
    }

    @Override
    public void onPause() {
        unRegisterChallengesListener();
    }

    @Override
    public void destroy() {
        unRegisterChallengesListener();
        if (!getActivity().isAdmin()) unRegisterDoneChallengesListener();
        if (mGps != null) mGps.stopUsingGPS();
        Rally.getInstance().setChallengeOpen(false);
        mContext = null;
        mFragment = null;
    }
}

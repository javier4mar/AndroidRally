package com.cbwmarketing.app.rally.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableInt;
import android.view.View;

import com.cbwmarketing.app.rally.BR;
import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.model.Game;
import com.cbwmarketing.app.rally.model.Resource;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.cbwmarketing.app.rally.view.ChronometerActivity;
import com.cbwmarketing.app.rally.view.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jcrawford on 7/11/2016.
 */

public class ChronometerViewModel extends BaseObservable implements ViewModel,
        ValueEventListener{

    public ObservableInt progressVisibility;
    private Context mContext;
    private Game mGame;
    private String btnStartText;
    private long mGameTime = 0;
    private boolean isAdmin;

    public ChronometerViewModel(Context context){
        this.mContext = context;
        this.progressVisibility = new ObservableInt(View.INVISIBLE);
        this.getImage();
        this.setButtonText();
        this.registerGameListener();
    }

    @Bindable
    public String getBtnStartText(){
        return btnStartText;
    }

    public void setBtnStartText(String text){
        this.btnStartText = text;
        notifyPropertyChanged(BR.btnStartText);
    }

    private void showProgress() {
        this.progressVisibility.set(View.VISIBLE);
    }

    private void hideProgress() {
        this.progressVisibility.set(View.INVISIBLE);
    }

    private void getImage(){
        getActivity().getCompanyImage(new BaseActivity.OnLoadCompanyImageListener() {
            @Override
            public void onLoadImage(List<Resource> resources) {
                for (Resource item : resources) {
                    if (item != null && item.getTitle().equalsIgnoreCase(BaseActivity.SPLASH_IMAGE)) {
                        getActivity().loadSplasImage(item.getUrl());
                        break;
                    }
                }
            }
        });
    }

    private void setButtonText() {
        isAdmin = getActivity().mPreferencesManager.getUserRol().equalsIgnoreCase(BaseActivity.ADMIN_ROL);
        if (isAdmin) setBtnStartText(mContext.getString(R.string.start_game_txt));
        else setBtnStartText(mContext.getString(R.string.play_game_txt));
    }

    private void registerGameListener() {
        showProgress();
        getActivity().mDatabase.child("games").addValueEventListener(this);
    }

    private void unRegisterGameListener(){
        getActivity().mDatabase.child("games").removeEventListener(this);
    }

    private void enableDisableButton(){
        if (isAdmin) getActivity().enableButtons();
        else getActivity().disableButtons();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        hideProgress();
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            mGame = postSnapshot.getValue(Game.class);
            if (mGame.getGameid() == getActivity().mPreferencesManager.getGameId()) {
                switch (mGame.getStatus()) {
                    case BaseActivity.STATUS_ACTIVE:
                        mGameTime = TimeUnit.MINUTES.toMillis(mGame.getTimeinminutes());
                        getActivity().initViews(mGameTime);
                        enableDisableButton();
                        break;
                    case BaseActivity.STATUS_RUNNING:
                        if (System.currentTimeMillis() < mGame.getFinishdate()) {
                            if (mGameTime > 0 && !isAdmin) {
                                final String message = mContext
                                        .getString(R.string.game_is_started_notification_msg);
                                BaseActivity.createNotification(mContext, message,
                                        BaseActivity.STATUS_RUNNING, ChronometerActivity.class);
                                mGameTime = 0;
                            } else
                                getActivity().initViews(getActivity().getCurrentTime(mGame));
                            getActivity().startChronometer();
                            getActivity().enableButtons();
                        } else {
                            getActivity().initViews(0);
                            getActivity().mDatabase.child("games").child(String.valueOf(mGame
                                    .getGameid())).child("status").setValue(BaseActivity.STATUS_FINISHED);
                        }
                        break;
                    case BaseActivity.STATUS_FINISHED:
                        mGameTime = TimeUnit.MINUTES.toMillis(mGame.getTimeinminutes());
                        getActivity().initViews(mGameTime);
                        enableDisableButton();
                        break;
                }
                break;
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        hideProgress();
    }

    public void onClick(View view) {
        if(mGame != null) {
            if (isAdmin && (mGame.getStatus() != BaseActivity.STATUS_RUNNING)) {
                final long startTime = System.currentTimeMillis();
                getActivity().mDatabase.child("games").child(String.valueOf(mGame.getGameid()))
                        .child("finishdate").setValue(startTime + mGameTime);
                getActivity().mDatabase.child("games").child(String.valueOf(mGame.getGameid()))
                        .child("startdate").setValue(startTime);
                getActivity().mDatabase.child("games").child(String.valueOf(mGame.getGameid()))
                        .child("status").setValue(BaseActivity.STATUS_RUNNING);
                getActivity().startChronometer();
                getActivity().disableButtons();
            }
            getActivity().mPreferencesManager.setIsGameStarted(true);
            getActivity().launchActivity(getActivity(), MainActivity.class, true);
        }
    }

    private ChronometerActivity getActivity() {
        return ((ChronometerActivity) mContext);
    }

    @Override
    public void destroy() {
        unRegisterGameListener();
        mGame = null;
        mContext = null;
    }
}

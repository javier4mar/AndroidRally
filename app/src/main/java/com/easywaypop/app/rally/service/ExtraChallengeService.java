package com.easywaypop.app.rally.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.utility.PreferencesManager;
import com.easywaypop.app.rally.view.BaseActivity;
import com.easywaypop.app.rally.view.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

public class ExtraChallengeService extends Service implements ValueEventListener {

    public static final String SERVICE_ACTION = "com.easywaypop.app.rally.service.ExtraChallengeService.SERVICE";
    private PreferencesManager mPreferencesManager;
    private DatabaseReference mDatabase;
    private Context mContext;

    public ExtraChallengeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = ExtraChallengeService.this;
        mPreferencesManager = new PreferencesManager(mContext);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerChallengesListener();
        return Service.START_REDELIVER_INTENT;
    }

    private void registerChallengesListener() {
        mDatabase.child("challenges").addValueEventListener(this);
    }

    private void unRegisterChallengesListener() {
        mDatabase.child("challenges").removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            final Challenge challenge = postSnapshot.getValue(Challenge.class);
            if (!isAdmin() && challenge.getGameid() == mPreferencesManager.getGameId()
                    && challenge.getVisible() == 1 && challenge.getStatus() == 2 &&
                    checkItWasNotified(postSnapshot.getKey())) {

                BaseActivity.createNotification(mContext, mContext.getString(
                        R.string.bonus_challenge_notification_msg), BaseActivity.STATUS_ACTIVE,
                        MainActivity.class);
                addChallengeNotified(challenge.getChallengeId());
             //   unRegisterChallengesListener();// stopSelf();

            }else if (!isAdmin() && challenge.getGameid() == mPreferencesManager.getGameId()
                    && challenge.getVisible() == 0 && challenge.getStatus() == 2 &&
                    checkItWasNotified(postSnapshot.getKey())) {

                deleteChallengeNotified(challenge.getChallengeId());

            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        unRegisterChallengesListener();
    }

    private boolean isAdmin() {
        return mPreferencesManager.getUserRol().equalsIgnoreCase(BaseActivity.ADMIN_ROL);
    }

    @Override
    public void onDestroy() {
        unRegisterChallengesListener();
        mDatabase = null;
        mPreferencesManager = null;
        mContext = null;
        super.onDestroy();
    }


    private boolean checkItWasNotified(String idChallenge) {

        Set<String> challenges = mPreferencesManager.getKeyExtraChallengesWasNotified();

        if (challenges.size() > 0) {

            if (challenges.contains(idChallenge)) {
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }

    }

    private void addChallengeNotified(String idChallenge) {

        Set<String> challenges = mPreferencesManager.getKeyExtraChallengesWasNotified();
        challenges.add(idChallenge);
        mPreferencesManager.setKeyIsExtraChallengeActivated(challenges);

    }

    private void deleteChallengeNotified(String idChallenge) {

        Set<String> challenges = mPreferencesManager.getKeyExtraChallengesWasNotified();
        challenges.remove(idChallenge);
        mPreferencesManager.setKeyIsExtraChallengeActivated(challenges);

    }

}

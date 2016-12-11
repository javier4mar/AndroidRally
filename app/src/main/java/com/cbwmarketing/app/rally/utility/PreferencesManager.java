package com.cbwmarketing.app.rally.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.cbwmarketing.app.rally.view.BaseActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to management share preferences
 */
public class PreferencesManager {
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // Shared pref mode
    private static final int PRIVATE_MODE = 0;

    /**
     * Sharedpref file name
     */
    private static final String PREF_NAME = "com.easywaypop.app.rally.EarthquakesDemoPref";


    private static final String KEY_IS_LINKED = "key_is_linked";

    private static final String KEY_GAME_ID = "key_game_id";

    private static final String KEY_USER_ROL = "key_user_rol";

    private static final String KEY_TEAM_ID = "key_team_id";

    private static final String KEY_IS_GAME_STARTED = "key_is_game_started";

    private static final String KEY_IS_EXTRA_CHALLENGE_ACTIVATED = "key_is_extra_challenge_activated";

    private static final String KEY_EXTRA_CHALLENGES_WAS_NOTIFIED= "key_is_extra_was_notified";

    private static final String KEY_PEDOMETER_COUNT= "key_pedometer_count";

    private static final String KEY_DEVICE_HAS_PEDOMETER= "key_has_pedometer";

    private static final String KEY_STEPS_IN_SENSOR_PEDOMETER= "key_steps_in_pedometer";

    private static final String KEY_SYSTEM_STEPS_DEVICE= "key_system_step_in_pedometer";

    private static final String KEY_KCAL_COUNT= "key_kcal_in_calculate";

    private static final String KEY_USER_FIREBASE_ID= "key_firebase_id_user";


    /**
     * Constructor
     * @param context current context
     */
    public PreferencesManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setLinked(boolean isLinked) {
        editor.putBoolean(KEY_IS_LINKED, isLinked);
        editor.apply();
    }

    public boolean isLinked() {
        return pref.getBoolean(KEY_IS_LINKED, false);
    }

    public void setKeyUserFirebaseId(String uidFirebase){
        editor.putString(KEY_USER_FIREBASE_ID, uidFirebase);
        editor.apply();
    }

    public String getFirebaseID(){
        return pref.getString(KEY_USER_FIREBASE_ID, "");
    }

    public void setGameId(long gameId){
        editor.putLong(KEY_GAME_ID, gameId);
        editor.apply();
    }

    public long getGameId(){
        return pref.getLong(KEY_GAME_ID, -1);
    }

    public void setUserRol(String userRol){
        editor.putString(KEY_USER_ROL, userRol);
        editor.apply();
    }

    public String getUserRol(){
        return pref.getString(KEY_USER_ROL, BaseActivity.ADMIN_ROL);
    }

    public void setTeamId(long teamId){
        editor.putLong(KEY_TEAM_ID, teamId);
        editor.apply();
    }

    public long getTeamId(){
        return pref.getLong(KEY_TEAM_ID, -1);
    }

    public void setIsGameStarted(boolean isGameStarted) {
        editor.putBoolean(KEY_IS_GAME_STARTED, isGameStarted);
        editor.apply();
    }

    public boolean isGameStarted() {
        return pref.getBoolean(KEY_IS_GAME_STARTED, false);
    }

    public void setIsExtraChallengeActivated(boolean isExtraChallengeActivated) {
        editor.putBoolean(KEY_IS_EXTRA_CHALLENGE_ACTIVATED, isExtraChallengeActivated);
        editor.apply();
    }

    public boolean isExtraChallengeActivated() {
        return pref.getBoolean(KEY_IS_EXTRA_CHALLENGE_ACTIVATED, false);
    }

    public void setKeyIsExtraChallengeActivated(Set<String> challenges) {
        editor.putStringSet(KEY_EXTRA_CHALLENGES_WAS_NOTIFIED,challenges);
        editor.apply();
    }

    public Set<String> getKeyExtraChallengesWasNotified() {
        return pref.getStringSet(KEY_EXTRA_CHALLENGES_WAS_NOTIFIED,new HashSet<String>());
    }

    public void logoutUser() {
        //editor.clear();
        editor.remove(KEY_USER_ROL);
        editor.remove(KEY_TEAM_ID);
        editor.remove(KEY_IS_GAME_STARTED);
        editor.remove(KEY_IS_EXTRA_CHALLENGE_ACTIVATED);
        editor.apply();
    }

    public String getKeyPedometerCount(){
        return pref.getString(KEY_PEDOMETER_COUNT,"0");
    }

    public void setKeyPedometerCount(String steps){

        editor.putString(KEY_PEDOMETER_COUNT, steps);
        editor.apply();
    }





    public void setHasPedometer(boolean hasPedometer) {
        editor.putBoolean(KEY_DEVICE_HAS_PEDOMETER, hasPedometer);
        editor.apply();
    }

    public boolean hasPedometer() {
        return pref.getBoolean(KEY_DEVICE_HAS_PEDOMETER, false);
    }





    public void setStepsInSensor(float localSteps){
        editor.putFloat(KEY_STEPS_IN_SENSOR_PEDOMETER, localSteps);
        editor.apply();
    }

    public float getStepsInSensor() {
        return pref.getFloat(KEY_STEPS_IN_SENSOR_PEDOMETER, 0);
    }



    public boolean getKeySystemSteps() {
        return pref.getBoolean(KEY_SYSTEM_STEPS_DEVICE, false);
    }

    public void setKeySystemSteps(boolean stepsON) {
        editor.putBoolean(KEY_SYSTEM_STEPS_DEVICE,stepsON);
        editor.apply();
    }


    public void setKeyKcalCount(String kcal){
        editor.putString(KEY_KCAL_COUNT, kcal);
        editor.apply();
    }

    public String getKeyKcalCount() {
        return pref.getString(KEY_KCAL_COUNT, "0");
    }

}
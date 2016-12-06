package com.cbwmarketing.app.rally.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cbwmarketing.app.rally.BuildConfig;
import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.utility.PreferencesManager;
import com.cbwmarketing.app.rally.view.MainActivity;

/**
 * Created by javierhernandez on 26/11/16.
 */

public class PedometerService extends Service implements SensorEventListener {

    private final static String TAG = PedometerService.class.getSimpleName();

    public static final String SERVICE_PEDOMETER_ACTION = "com.cbwmarketing.app.rally.service.PedometerService.SERVICE";
    public static final String PEDOMETER_ACTION = "PedometerService.SERVICE";
    public static final String STEP_NUMBER = "steps";
    public static final String KCAL_NUMBER = "kcal";

    private PreferencesManager mPreferencesManager;
    private Context mContext;
    private SensorManager sensorManager;


    public PedometerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reRegisterSensor();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.i(TAG, "SensorListener onCreate");
        mContext = PedometerService.this;
        mPreferencesManager = new PreferencesManager(mContext);
        Log.e(TAG, "SensorListener onCreate");
    }

    private void reRegisterSensor() {
        if (BuildConfig.DEBUG) Log.i(TAG, "re-register sensor listener");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            mPreferencesManager.setHasPedometer(true);

            if (BuildConfig.DEBUG)
                Log.i(TAG, "device: " + countSensor.getName() + " " + countSensor.getType());

        } else {
            sendBroadCastPedometer(null);
        }

    }

    private void sendBroadCastPedometer(Bundle bundle) {

        Intent intent = new Intent(MainActivity.GET_PEDOMETER_BROADCAST);
        intent.putExtra(PEDOMETER_ACTION, bundle);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(mPreferencesManager.hasPedometer()){

           float finalStep=  event.values[0] - loadStepsInSensor(event.values[0]);
           double kcal = calculateCaloriesBurned(finalStep);

            Bundle inf = new Bundle();
            inf.putString(STEP_NUMBER, String.valueOf(finalStep));
            inf.putString(KCAL_NUMBER,String.valueOf(kcal));
            sendBroadCastPedometer(inf);

            saveValuesCounter(String.valueOf(finalStep),String.valueOf(kcal));

            if (BuildConfig.DEBUG) Log.e(TAG, "Steps" + finalStep);Log.e(TAG, " cal" + String.valueOf(kcal));

        }else{
            if (BuildConfig.DEBUG) Log.e(TAG, getString(R.string.your_device_dont_have_pedometer));
        }

    }

    public float loadStepsInSensor(float steps){

        if(!mPreferencesManager.getKeySystemSteps()){
            mPreferencesManager.setStepsInSensor(steps);
            mPreferencesManager.setKeySystemSteps(true);
        }

        return  mPreferencesManager.getStepsInSensor();

    }

    public double calculateCaloriesBurned(float steps){

        /*
            1.)Calories burned per mile = 0.57 x 175 lbs.(your weight) = 99.75 calories per mile.

            2.)Your_strip = height * 0,415.

            3.) steps_in_1_mile = 160934.4(mile in cm) / strip.

            4.) "conversationFactor" = stepsCount (what the pedometer provides) / step_in_1_mile;

            5.) CaloriesBurned = stepsCount * conversationFactor;*/

        double caloriesBurned = 99.75;
        double strip  = 170 * 0.415;
        double steps_in_1_mile = 160934.4 / strip;
        double conversationFactor = steps / steps_in_1_mile;
        double Calories =steps * conversationFactor;

        return Math.round(Math.floor(Calories));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.i(TAG, "SensorListener onDestroy");
        try {
            resetValuesCounter();
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private void resetValuesCounter(){
        mPreferencesManager.setKeySystemSteps(false);
        mPreferencesManager.setKeyPedometerCount("0");
        mPreferencesManager.setKeyKcalCount("0");
    }

    private void saveValuesCounter(String steps , String kcal){
        mPreferencesManager.setKeySystemSteps(true);
        mPreferencesManager.setKeyPedometerCount(steps);
        mPreferencesManager.setKeyKcalCount(kcal);
    }

}

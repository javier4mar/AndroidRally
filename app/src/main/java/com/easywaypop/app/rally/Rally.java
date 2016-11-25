package com.easywaypop.app.rally;

import android.support.multidex.MultiDexApplication;

import com.easywaypop.app.rally.model.Team;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * Created by Juan-Crawford on 6/11/2016.
 */

public class Rally extends MultiDexApplication {

    private static Rally mInstance;
    private boolean isChallengeOpen = false;
    private String mTeamName;
    private Team mTeamWinner;

    public static synchronized Rally getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = Rally.this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
    }

    public boolean isChallengeOpen() {
        return isChallengeOpen;
    }

    public void setChallengeOpen(boolean challengeOpen) {
        isChallengeOpen = challengeOpen;
    }

    public String getTeamName() {
        return mTeamName;
    }

    public void setTeamName(String teamName) {
        this.mTeamName = teamName;
    }

    public Team getTeamWinner() {
        return mTeamWinner;
    }

    public void setTeamWinner(Team teamWinner) {
        this.mTeamWinner = teamWinner;
    }
}

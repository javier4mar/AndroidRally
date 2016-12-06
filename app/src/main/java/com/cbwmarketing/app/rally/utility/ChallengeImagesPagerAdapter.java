package com.cbwmarketing.app.rally.utility;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cbwmarketing.app.rally.model.Image;
import com.cbwmarketing.app.rally.view.ChallengeImageFragment;

import java.util.List;

public class ChallengeImagesPagerAdapter extends FragmentStatePagerAdapter {

    private List<Image> mImagesList;

    public ChallengeImagesPagerAdapter(FragmentManager fm, List<Image> images) {
        super(fm);
        this.mImagesList = images;
    }

    @Override
    public Fragment getItem(int position) {
        return ChallengeImageFragment.newInstance(mImagesList.get(position).getUrl());
    }

    @Override
    public int getCount() {
        return mImagesList.size();
    }
}
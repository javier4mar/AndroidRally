package com.easywaypop.app.rally.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.MenuItem;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.databinding.ActivityChallengeBinding;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.model.Image;
import com.easywaypop.app.rally.utility.ChallengeImagesPagerAdapter;

import java.util.List;

public class ChallengeActivity extends AppCompatActivity {

    private static final String EXTRA_CHALLENGE= "com.easywaypop.app.rally.extraChallenge";
    private static final String EXTRA_DONE_CHALLENGE = "com.easywaypop.app.rally.extraDoneChallenge";
    private ActivityChallengeBinding mBinding;

    public static void navigate(AppCompatActivity activity, Challenge challenge, DoneChallenge doneChallenge) {
        Intent intent = new Intent(activity, ChallengeActivity.class);
        intent.putExtra(EXTRA_CHALLENGE, challenge);
        intent.putExtra(EXTRA_DONE_CHALLENGE, doneChallenge);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_challenge);
        initViews();
    }

    public void setToolbarTitle(final String title){
        mBinding.appbarlayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1)
                    scrollRange = appBarLayout.getTotalScrollRange();
                if (scrollRange + verticalOffset == 0) {
                    mBinding.toolbar.setTitle(title);
                    mBinding.toolbar.setTitleTextColor(Color.WHITE);
                    isShow = true;
                } else if (isShow) {
                    mBinding.toolbar.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    private void initViews(){
        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(null);
        }
        mBinding.collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        mBinding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        mBinding.collapsingToolbar.setTitleEnabled(false);
        final Challenge challenge = getIntent().getParcelableExtra(EXTRA_CHALLENGE);
        final DoneChallenge doneChallenge = getIntent().getParcelableExtra(EXTRA_DONE_CHALLENGE);
        if(challenge != null && doneChallenge != null) {
            initViewpager(addDoneChallengeImage(doneChallenge, challenge.getImages()));
            setToolbarTitle(challenge.getTitle());
            mBinding.tvTitle.setText(challenge.getTitle());
            mBinding.tvWins.setText(BaseActivity.getMessage(String.format(getString(R.string.arrive_format),
                    BaseActivity.formatHour(doneChallenge.getDatestarted()))));
            mBinding.tvLeft.setText(BaseActivity.getMessage(String.format(getString(R.string.finished_format),
                    BaseActivity.formatHour(doneChallenge.getDatefinish()))));
            mBinding.tvInstructions.setText(BaseActivity.getMessage(challenge.getBody()));
            mBinding.tvInstructions.setMovementMethod(new LinkMovementMethod());
            Linkify.addLinks( mBinding.tvInstructions, Patterns.WEB_URL, "http://");
        }
    }

    private List<Image> addDoneChallengeImage(DoneChallenge doneChallenge, List<Image> images){
        if(!ifExistsDoneChallengeImage(doneChallenge, images)) {
            Image image = new Image();
            image.setName(doneChallenge.getResources().getTitle());
            image.setStatus(1);
            image.setTitle("Done Challenge Image");
            image.setType(doneChallenge.getResources().getType());
            image.setUrl(doneChallenge.getResources().getUrl());
            images.add(0, image);
        }
        return images;
    }

    private boolean ifExistsDoneChallengeImage(DoneChallenge doneChallenge, List<Image> images){
        for (Image item : images){
            if(doneChallenge.getResources().getUrl().equals(item.getUrl()))
                return true;
        }
        return false;
    }

    private void initViewpager(List<Image> images){
        final ViewPager viewpager = mBinding.pager;
        viewpager.setAdapter(new ChallengeImagesPagerAdapter(getSupportFragmentManager(),
                images));
        mBinding.indicator.setViewPager(viewpager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

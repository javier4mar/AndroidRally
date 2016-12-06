package com.cbwmarketing.app.rally.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.databinding.ActivityTeamBinding;

public class TeamActivity extends BaseActivity {

    private static final String EXTRA_FRAGMENT = "com.cbwmarketing.app.rally.extraFragment";
    private ActivityTeamBinding mBinding;

    public static void navigate(AppCompatActivity activity, String fragment) {
        Intent intent = new Intent(activity, TeamActivity.class);
        intent.putExtra(EXTRA_FRAGMENT, fragment);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_team);
        initViews();
    }

    private void initViews() {
        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(null);
        }
        mBinding.collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        mBinding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        setContentFragment(getIntent().getStringExtra(EXTRA_FRAGMENT));
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

    private void setContentFragment(final String fragmentId) {
        Fragment fragment = null;
        switch (fragmentId) {
            case "ScoreboardFragment":
                fragment = new ScoreboardFragment();
                break;
            case "TeamFragment":
                fragment = TeamFragment.newInstance(mPreferencesManager.getTeamId());
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.flContainer, fragment, fragmentId).commit();
        }
    }

    public void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = TeamActivity.this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_right_exit);
        fragmentTransaction.replace(R.id.flContainer, fragment,
                fragment.getClass().getSimpleName());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    public ActivityTeamBinding getBinding() {
        return mBinding;
    }
}

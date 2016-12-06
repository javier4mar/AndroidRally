package com.cbwmarketing.app.rally.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.Rally;
import com.cbwmarketing.app.rally.databinding.ActivityMainBinding;
import com.cbwmarketing.app.rally.model.Team;
import com.cbwmarketing.app.rally.service.PedometerService;
import com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike;
import com.cbwmarketing.app.rally.utility.MergedAppBarLayoutBehavior;
import com.cbwmarketing.app.rally.viewmodel.MainViewModel;

import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.State;
import static com.cbwmarketing.app.rally.utility.BottomSheetBehaviorGoogleMapsLike.from;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final String GET_PEDOMETER_BROADCAST ="com.cbwmarketing.app.rally.getPedometerBroadCast" ;

    private static final long DRAWER_CLOSE_DELAY_MS = 300;
    private static final long ANIMATION_DURATION = 50;
    private static final String NAV_ITEM_ID = "navItemId";
    private ActivityMainBinding mBinding;
    private MainViewModel mMainViewModel;
    private ActionBarDrawerToggle mDrawerToggle;
    private MergedAppBarLayoutBehavior mAppBarLayoutBehavior;
    private BottomSheetBehaviorGoogleMapsLike mBottomSheetBehavior;
    private CountDownTimer mCountDownTimer;
    private final Handler mDrawerActionHandler = new Handler();
    private boolean isExpand = false;
    private long mReminderTime = 0;
    private int mNavItemId;
    private LocalBroadcastManager mLocalBroadcastManager;


    private BroadcastReceiver mPedometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle mPedometer = intent.getParcelableExtra(PedometerService.
                    PEDOMETER_ACTION);
            if(mPedometer != null){
                setStepAndKcl(mPedometer.getString(PedometerService.STEP_NUMBER),mPedometer.getString(PedometerService.KCAL_NUMBER));
            }else{
                setNoPedometerAvailable();
                MainActivity.this.showToastMessage(getString(R.string.your_device_dont_have_pedometer));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainViewModel = new MainViewModel(this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);

        if (null == savedInstanceState) mNavItemId = R.id.action_home;
        else mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        initViews();
        registerBroadcastReceiver();
        initStepKcalCount();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && getMapFragment() == null) {
            mNavItemId = R.id.action_home;
            navigate(mNavItemId);
        }
    }


    private void setUpFragments() {
        MapFragment mapFragment = new MapFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_map, mapFragment,
                mapFragment.getClass().getSimpleName()).commit();
    }

    private void initViews() {
        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("");
        }
        ViewCompat.setElevation(mBinding.toolbar, TypedValue.applyDimension(TypedValue.
                COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mBinding.tvToolbarTitle.setText(getString(R.string.app_name));
        initDrawer(mPreferencesManager.getUserRol().equalsIgnoreCase(BaseActivity.ADMIN_ROL));
        mBottomSheetBehavior = from(mBinding.bottomSheet);
        hideBottomSheet();
        mBottomSheetBehavior.addBottomSheetCallback(mBottomSheetCallback);
        mAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mBinding.mergedAppbarlayout);
        changeTitleOfBottomSheet("");
        mAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(STATE_COLLAPSED);
            }
        });
        setUpFragments();
    }

    public void initDrawer(boolean isAdmin) {
        if (isAdmin) mBinding.navigationView.inflateMenu(R.menu.menu_drawer_moderator);
        else mBinding.navigationView.inflateMenu(R.menu.menu_drawer_player);
        mBinding.navigationView.setNavigationItemSelectedListener(this);
        mBinding.navigationView.getMenu().findItem(mNavItemId).setChecked(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mBinding.drawerLayout, mBinding.toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mMainViewModel.getTeamInfo();
    }

    public void setInfoInDrawer(final Team team) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View headerView = mBinding.navigationView.inflateHeaderView(R.layout.content_drawer_header);
                final ImageView ivAvatar = (ImageView) headerView.findViewById(R.id.avatar);
                final TextView tvTeam = (TextView) headerView.findViewById(R.id.team);
                Rally.getInstance().setTeamName(team.getName());
                if (tvTeam != null && ivAvatar != null) {
                    tvTeam.setText(team.getName());
                    BaseActivity.loadImage(MainActivity.this, team.getTeamimage(), ivAvatar);
                }
            }
        });
    }


    public void setStepAndKcl(String stepCounter , String kcal) {
        //Show steps and kcal
        //mBinding.tvKal.setText(String.valueOf(stepCounter) +" "+ getString(R.string.steps_label)+" / "+ kcal +" "+ getString(R.string.calories_label));

       //Show only kcal
        mBinding.tvKal.setText( kcal +" "+ getString(R.string.calories_label));

    }


    public void setNoPedometerAvailable() {
        mBinding.tvKal.setText(getString(R.string.counter_step_no_available));

    }

    private void registerBroadcastReceiver(){
        mLocalBroadcastManager.registerReceiver(mPedometerReceiver,
                new IntentFilter(GET_PEDOMETER_BROADCAST));
    }

    private void unRegisterBroadcastReceiver(){
        mLocalBroadcastManager.unregisterReceiver(mPedometerReceiver);
    }

    public void initChronometer(long time) {
        mBinding.tvTime.setText(BaseActivity.formatChronometer(time));
        mCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mReminderTime = millisUntilFinished;
                mBinding.tvTime.setText(BaseActivity.formatChronometer(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                mReminderTime = 0;
                mBinding.tvTime.setText(R.string.finish_time_text);
                mMainViewModel.finishGameByTime();
            }
        };
        if (time > 0) mCountDownTimer.start();
    }

        private  void initStepKcalCount(){

            setStepAndKcl(mPreferencesManager.getKeyPedometerCount(),mPreferencesManager.getKeyKcalCount());

        }

    private BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback mBottomSheetCallback = new BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @State int newState) {
            switch (newState) {
                case STATE_COLLAPSED:
                    setBottomSheetWhite(newState);
                    if(isExpand) {
                        hideBottomSheet();
                        isExpand = false;
                    }
                    break;
                case STATE_DRAGGING:
                    setBottomSheetBlue();
                    break;
                case STATE_EXPANDED:
                    break;
                case STATE_ANCHOR_POINT:
                    switchChallengeTexts(newState);
                    break;
                case STATE_HIDDEN:
                    break;
                case STATE_SETTLING:
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    private void switchChallengeTexts(int newState) {
        MapFragment mapFragment = getMapFragment();
        if (mapFragment != null) {
            switch (newState) {
                case STATE_COLLAPSED:
                    mapFragment.setInfoInBottomSheetWhenCollapsed();
                    break;
                case STATE_ANCHOR_POINT:
                    mapFragment.setInfoInBottomSheetWhenAnchorPoint();
                    break;
            }
        }
    }

    private void setBottomSheetBlue() {
        if (getBackgroundColor(mBinding.included.llHeader) != ContextCompat.
                getColor(MainActivity.this, R.color.colorPrimary)) {
            changeColorAnimation(mBinding.included.llHeader,
                    ContextCompat.getColor(MainActivity.this, R.color.White),
                    ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
            changeColorAnimation(mBinding.included.tvBSTitle,
                    ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryText),
                    ContextCompat.getColor(MainActivity.this, R.color.White));
            changeColorAnimation(mBinding.included.tvBSWins,
                    ContextCompat.getColor(MainActivity.this, R.color.colorSecondaryText),
                    ContextCompat.getColor(MainActivity.this, R.color.White));
            changeColorAnimation(mBinding.included.tvBSReminder,
                    ContextCompat.getColor(MainActivity.this, R.color.colorSecondaryText),
                    ContextCompat.getColor(MainActivity.this, R.color.White));
        }
    }

    private void setBottomSheetWhite(int newState) {
        if (getBackgroundColor(mBinding.included.llHeader) != ContextCompat.
                getColor(MainActivity.this, R.color.White)) {
            switchChallengeTexts(newState);
            changeColorAnimation(mBinding.included.llHeader,
                    ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                    ContextCompat.getColor(MainActivity.this, R.color.White));
            changeColorAnimation(mBinding.included.tvBSTitle,
                    ContextCompat.getColor(MainActivity.this, R.color.White),
                    ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryText));
            changeColorAnimation(mBinding.included.tvBSWins,
                    ContextCompat.getColor(MainActivity.this, R.color.White),
                    ContextCompat.getColor(MainActivity.this, R.color.colorSecondaryText));
            changeColorAnimation(mBinding.included.tvBSReminder,
                    ContextCompat.getColor(MainActivity.this, R.color.White),
                    ContextCompat.getColor(MainActivity.this, R.color.colorSecondaryText));
        }
    }

    private void changeColorAnimation(final View view, int colorFrom, int colorTo) {
        ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        anim.setDuration(ANIMATION_DURATION);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if (view instanceof TextView)
                    ((TextView) view).setTextColor((int) animator.getAnimatedValue());
                else
                    view.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        anim.start();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        if (!item.isChecked()) {
            if (item.getItemId() == R.id.action_home) item.setChecked(true);
            mNavItemId = item.getItemId();
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
            mDrawerActionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(item.getItemId());
                }
            }, DRAWER_CLOSE_DELAY_MS);
        } else
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    public void navigate(final int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.action_home:
                fragment = new MapFragment();
                break;
            case R.id.action_team:
            case R.id.action_score:
                TeamActivity.navigate(MainActivity.this, "ScoreboardFragment");
                break;
            case R.id.action_history:
                TeamActivity.navigate(MainActivity.this, "TeamFragment");
                break;
            case R.id.action_help:
                mMainViewModel.confirmHelpRequest();
                break;
            case R.id.action_bonus_challenge:
                mMainViewModel.confirmActiveBonusChallenge();
                break;
            case R.id.action_logout:
                logOut();
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_map, fragment,
                    fragment.getClass().getSimpleName()).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(NAV_ITEM_ID, mNavItemId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        else if (isBottomSheetVisible())
            hideBottomSheet();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fl_map);
        if (mapFragment != null)
            mapFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fl_map);
        if (mapFragment != null) mapFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainViewModel.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainViewModel.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        unRegisterBroadcastReceiver();
        mMainViewModel.destroy();
        super.onDestroy();
    }

    public MapFragment getMapFragment() {
        return (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fl_map);
    }

    public void changeTitleOfBottomSheet(String title) {
        if (mAppBarLayoutBehavior != null) mAppBarLayoutBehavior.setToolbarTitle(title);
    }

    public void showBottomSheetCollapsed() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setHideable(false);
            mBottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    public void setBottomSheetCollapsed() {
        if (mBottomSheetBehavior != null) mBottomSheetBehavior.setState(STATE_COLLAPSED);
    }

    public void hideBottomSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setHideable(true);
            mBottomSheetBehavior.setState(STATE_HIDDEN);
            isExpand = false;
        }
    }

    public void expandBottomSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(STATE_ANCHOR_POINT);
            setBottomSheetBlue();
            isExpand = true;
        }
    }

    private boolean isBottomSheetVisible() {
        return mBottomSheetBehavior.getState() == STATE_COLLAPSED ||
                mBottomSheetBehavior.getState() == STATE_ANCHOR_POINT ||
                mBottomSheetBehavior.getState() == STATE_EXPANDED;
    }

    public boolean isNeedCollapsed() {
        return mBottomSheetBehavior.getState() != STATE_COLLAPSED;
    }

    public long getReminderTime() {
        return mReminderTime;
    }

    public ActivityMainBinding getBinding() {
        return mBinding;
    }
}

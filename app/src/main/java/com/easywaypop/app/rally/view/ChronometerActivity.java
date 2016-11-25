package com.easywaypop.app.rally.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.databinding.ActivityChronometerBinding;
import com.easywaypop.app.rally.viewmodel.ChronometerViewModel;

public class ChronometerActivity extends BaseActivity {

    private ActivityChronometerBinding mBinding;
    private ChronometerViewModel mChronometerViewModel;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chronometer);
        mChronometerViewModel = new ChronometerViewModel(this);
        mBinding.setViewModel(mChronometerViewModel);
        initViews();
    }

    private void initViews() {
        setSupportActionBar(mBinding.toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(null);
    }

    public void loadSplasImage(String url){
        BaseActivity.loadImage(this, url, mBinding.ivSplash);
    }

    public void initViews(long time) {
        mBinding.chronometer.setText(formatChronometer(time));
        mCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mBinding.chronometer.setText(formatChronometer(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                mBinding.chronometer.setText(R.string.finish_time_text);
            }
        };
    }

    public void startChronometer(){
        if(mCountDownTimer != null) mCountDownTimer.start();
    }

    public void disableButtons() {
        if (mBinding.btnStart != null) mBinding.btnStart.setEnabled(false);
    }

    public void enableButtons() {
        if (mBinding.btnStart != null) mBinding.btnStart.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chronometer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCountDownTimer != null) mCountDownTimer.cancel();
        mChronometerViewModel.destroy();
    }
}

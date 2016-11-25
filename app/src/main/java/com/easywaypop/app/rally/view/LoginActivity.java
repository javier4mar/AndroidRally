package com.easywaypop.app.rally.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;

import com.easywaypop.app.rally.R;


public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_login);
        setStatusBarBackgroundBlue();
        setFragment();
    }

    private void setStatusBarBackgroundBlue(){
        if (BaseActivity.isLollipop()) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    private void setFragment() {
        Fragment fragment;
        if (mPreferencesManager.isLinked()) fragment = new LoginFragment();
        else fragment = new CompanyFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment,
                fragment.getClass().getSimpleName()).commit();
    }

    public void changeFragment(final Fragment fragment, final int orientation) {
        FragmentManager fragmentManager = LoginActivity.this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (orientation == 1) {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_right_exit);
        } else {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_right_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_left_exit);
        }
        fragmentTransaction.replace(R.id.flContainer, fragment,
                fragment.getClass().getSimpleName());
        fragmentTransaction.commitAllowingStateLoss();
    }
}

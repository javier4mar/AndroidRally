package com.cbwmarketing.app.rally.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.databinding.FragmentLoginBinding;
import com.cbwmarketing.app.rally.viewmodel.LoginViewModel;
import com.cbwmarketing.app.rally.viewmodel.MainViewModelContract;

/**
 * Created by Juan-Crawford on 6/11/2016.
 */

public class LoginFragment extends Fragment implements MainViewModelContract.MainView {

    private LoginActivity mActivity;
    private LoginViewModel mLoginViewModel;
    private FragmentLoginBinding mBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (LoginActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        mLoginViewModel = new LoginViewModel(this, mActivity);
        mBinding.setViewModel(mLoginViewModel);
        return mBinding.getRoot();
    }

    @Override
    public LoginFragment getFragment() {
        return LoginFragment.this;
    }

    public void initViews(String url){
        BaseActivity.loadImage(mActivity, url, mBinding.ivLogo);
    }

    public boolean validateLoginForm(String user, String pwd) {
        if (user == null || user.isEmpty()) {
            mBinding.etUser.requestFocus();
            mBinding.etUser.setError(mActivity.getString(R.string.user_required_mg));
            return false;
        } else if (pwd == null || pwd.isEmpty()) {
            mBinding.etPwd.requestFocus();
            mBinding.etPwd.setError(mActivity.getString(R.string.pwd_required_msg));
            return false;
        } else
            return true;
    }

    public void clearFields(boolean flag) {
        if (flag && mBinding.etUser != null && mBinding.etPwd != null) {
            mBinding.etUser.setText("");
            mBinding.etPwd.setText("");
        } else if (mBinding.etPwd != null) {
            mBinding.etPwd.setText("");
        }
    }

    public void disableButtons() {
        if (mBinding.btnLogin != null) mBinding.btnLogin.setEnabled(false);
    }

    public void enableButtons() {
        if (mBinding.btnLogin != null) mBinding.btnLogin.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLoginViewModel.destroy();
    }
}

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
import com.cbwmarketing.app.rally.databinding.FragmentCompanyBinding;
import com.cbwmarketing.app.rally.viewmodel.CompanyViewModel;
import com.cbwmarketing.app.rally.viewmodel.MainViewModelContract;

/**
 * Created by Juan-Crawford on 6/11/2016.
 */

public class CompanyFragment extends Fragment implements MainViewModelContract.MainView{

    private LoginActivity mActivity;
    private CompanyViewModel mCompanyViewModel;
    private FragmentCompanyBinding mBinding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (LoginActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_company, container, false);
        mCompanyViewModel = new CompanyViewModel(this, mActivity);
        mBinding.setViewModel(mCompanyViewModel);
        return mBinding.getRoot();
    }

    @Override
    public CompanyFragment getFragment() {
        return CompanyFragment.this;
    }

    public boolean validateForm(String alias) {
        if (alias == null || alias.isEmpty()) {
            mBinding.etAlias.requestFocus();
            mBinding.etAlias.setError(mActivity.getString(R.string.company_name_required_msg));
            return false;
        } else
            return true;
    }

    public void clearEditText() {
        if (mBinding.etAlias != null) mBinding.etAlias.setText("");
    }

    public void disableButtons() {
        if (mBinding.btnLink != null) mBinding.btnLink.setEnabled(false);
    }

    public void enableButtons() {
        if (mBinding.btnLink != null) mBinding.btnLink.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompanyViewModel.destroy();
    }
}

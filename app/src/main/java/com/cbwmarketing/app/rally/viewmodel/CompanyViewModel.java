package com.cbwmarketing.app.rally.viewmodel;

import android.content.Context;
import android.databinding.ObservableInt;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.model.Game;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.cbwmarketing.app.rally.view.CompanyFragment;
import com.cbwmarketing.app.rally.view.LoginActivity;
import com.cbwmarketing.app.rally.view.LoginFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Juan-Crawford on 6/11/2016.
 */

public class CompanyViewModel implements ViewModel {

    public ObservableInt progressVisibility;
    private Context mContext;
    private CompanyFragment mFragment;
    private String mAlias;

    public CompanyViewModel(MainViewModelContract.MainView mainView, Context context) {
        this.mContext = context;
        this.mFragment = (CompanyFragment) mainView.getFragment();
        this.progressVisibility = new ObservableInt(View.INVISIBLE);
    }

    public TextWatcher getAliasEditTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mAlias = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    public void onClick(View view) {
        doLink();
    }

    private void showProgress() {
        this.progressVisibility.set(View.VISIBLE);
    }

    private void hideProgress() {
        this.progressVisibility.set(View.INVISIBLE);
    }

    private void doLink() {
        if (BaseActivity.haveNetworkConnection(mContext)) {
            if (mFragment.validateForm(mAlias)) {
                BaseActivity.hideSoftKeyboard(getActivity());
                showProgress();
                mFragment.disableButtons();
                getActivity().mDatabase.child("games").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try {
                            hideProgress();
                            mFragment.enableButtons();
                            boolean isFound = false;
                            Game game = null;
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                game = postSnapshot.getValue(Game.class);
                                if (game.getAlias().equalsIgnoreCase(mAlias) ||
                                        game.getCompany().equalsIgnoreCase(mAlias) ||
                                        game.getName().equalsIgnoreCase(mAlias)) {
                                    isFound = true;
                                    break;
                                }
                            }
                            mFragment.clearEditText();
                            if (!isFound) {
                                getActivity().showGeneralAlertDialog(mContext.getString(R.string.company_not_found_msg));
                            } else {
                                getActivity().mPreferencesManager.setLinked(true);
                                getActivity().mPreferencesManager.setGameId(game.getGameid());
                                getActivity().changeFragment(new LoginFragment(), 1);
                            }
                        }catch (Exception e){

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgress();
                        mFragment.enableButtons();
                    }
                });
            }
        } else
            getActivity().showGeneralAlertDialog(getActivity().getString(R.string.no_connected_msg));
    }

    private LoginActivity getActivity() {
        return ((LoginActivity) mContext);
    }

    @Override
    public void destroy() {
        mContext = null;
        mFragment = null;
    }
}

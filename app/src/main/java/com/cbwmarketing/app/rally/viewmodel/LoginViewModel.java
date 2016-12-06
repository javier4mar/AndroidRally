package com.cbwmarketing.app.rally.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.model.Resource;
import com.cbwmarketing.app.rally.model.User;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.cbwmarketing.app.rally.view.ChronometerActivity;
import com.cbwmarketing.app.rally.view.CompanyFragment;
import com.cbwmarketing.app.rally.view.LoginActivity;
import com.cbwmarketing.app.rally.view.LoginFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


/**
 * Created by Juan-Crawford on 6/11/2016.
 */

public class LoginViewModel implements ViewModel {

    public ObservableInt progressVisibility;
    private Context mContext;
    private LoginFragment mFragment;
    private String mUserName;
    private String mPassword;

    public LoginViewModel(MainViewModelContract.MainView mainView, Context context) {
        this.mContext = context;
        this.mFragment = (LoginFragment) mainView.getFragment();
        this.progressVisibility = new ObservableInt(View.INVISIBLE);
        this.getImage();
    }

    public TextWatcher getUsernameEditTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mUserName = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    public TextWatcher getPasswordEditTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mPassword = charSequence.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    private void showProgress() {
        this.progressVisibility.set(View.VISIBLE);
    }

    private void hideProgress() {
        this.progressVisibility.set(View.INVISIBLE);
    }

    private void getImage(){
        getActivity().getCompanyImage(new BaseActivity.OnLoadCompanyImageListener() {
            @Override
            public void onLoadImage(List<Resource> resources) {
                for (Resource item : resources) {
                    if (item != null && item.getTitle().equalsIgnoreCase(BaseActivity.LOGIN_IMAGE)) {
                        mFragment.initViews(item.getUrl());
                        break;
                    }
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnLogin:
                doLogin();
                break;
            case R.id.tvUnlink:
                doUnlink();
                break;
        }
    }

    private void doUnlink() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.confirmation_label))
                .setMessage(mContext.getString(R.string.unlink_confirmation_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().mPreferencesManager.setLinked(false);
                        getActivity().changeFragment(new CompanyFragment(), 0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void doLogin() {
        if (BaseActivity.haveNetworkConnection(mContext)) {
            if (mFragment.validateLoginForm(mUserName, mPassword)) {
                String user = mUserName;
                if (!getActivity().isValidMail(user)) user = getActivity().getUsrEmail(user);
                BaseActivity.hideSoftKeyboard(getActivity());
                showProgress();
                mFragment.disableButtons();
                getActivity().mFirebaseAuth.signInWithEmailAndPassword(user, mPassword)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    loadGeneralData();
                                } else {
                                    hideProgress();
                                    mFragment.enableButtons();
                                    mFragment.clearFields(false);
                                    getActivity().showGeneralAlertDialog(getActivity()
                                            .getString(R.string.login_error_msg));
                                }
                            }
                        });
            }
        } else
            getActivity().showGeneralAlertDialog(getActivity().getString(R.string.no_connected_msg));
    }

    private void loadGeneralData() {
        getActivity().mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final FirebaseUser firebaseUser = getActivity().mFirebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    final String userId = firebaseUser.getUid();
                    hideProgress();
                    mFragment.enableButtons();
                    mFragment.clearFields(true);
                    final User user = dataSnapshot.child("users").child(userId).getValue(User.class);
                    if (user.getRol() != null) {
                        getActivity().mPreferencesManager.setUserRol(user.getRol());
                        getActivity().mPreferencesManager.setTeamId(user.getTeamid());
                        getActivity().launchActivity(getActivity(), ChronometerActivity.class, false);
                    } else {
                        getActivity().showGeneralAlertDialog(getActivity().
                                getString(R.string.user_have_not_permissons_error_msg));
                        getActivity().mFirebaseAuth.signOut();
                    }
                }
                getActivity().mDatabase.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgress();
                mFragment.enableButtons();
                mFragment.clearFields(false);
                getActivity().showGeneralAlertDialog(getActivity()
                        .getString(R.string.connection_general_error_msg));
                getActivity().mDatabase.removeEventListener(this);
                getActivity().mFirebaseAuth.signOut();
            }
        });
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

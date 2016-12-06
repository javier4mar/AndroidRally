package com.cbwmarketing.app.rally.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.Rally;
import com.cbwmarketing.app.rally.model.ScoreboardItem;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.cbwmarketing.app.rally.view.TeamActivity;
import com.cbwmarketing.app.rally.view.TeamFragment;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class ItemScoreboardViewModel extends BaseObservable {

    public ObservableInt buttonVisibility;

    private Context mContext;
    private ScoreboardItem mScoreboardItem;
    private int mPosition;

    public ItemScoreboardViewModel(Context context, ScoreboardItem scoreboardItem, int position){
        this.mContext = context;
        this.mScoreboardItem = scoreboardItem;
        this.mPosition = position;
        this.buttonVisibility = new ObservableInt(View.GONE);
        validateButton();
    }

    private void showButton(){
        this.buttonVisibility.set(View.VISIBLE);
    }

    private void hideButton(){
        this.buttonVisibility.set(View.GONE);
    }

    private void validateButton(){
       if(getActivity().isAdmin() && mPosition == 0) showButton();
        else hideButton();
    }

    @Bindable
    public String getTeam() {
        return mScoreboardItem.getTeam().getName();
    }

    @Bindable
    public int getTextColorTeam(){
        return getColor();
    }

    @Bindable
    public Spanned getWins(){
        return BaseActivity.getMessage(String.format(mContext.getString(R.string.wins_format),
                mScoreboardItem.getDoneChallengeList().size()));
    }

    @Bindable
    public String getPictureTeam() {
        return mScoreboardItem.getTeam().getTeamimage();
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        BaseActivity.loadImage(imageView.getContext(), url, imageView);
    }

    public void onItemClick(View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().changeFragment(TeamFragment.
                        newInstance(mScoreboardItem.getTeam().getTeamid()));
            }
        }, 200);
    }

    public void onClick(View view){
        confirmSetWinner();
    }

    private int getColor(){
        if(!getActivity().isAdmin() && getActivity().mPreferencesManager.getTeamId() ==
                mScoreboardItem.getTeam().getTeamid())
            return ContextCompat.getColor(mContext, R.color.greenArrow);
        else
            return ContextCompat.getColor(mContext, R.color.colorPrimaryText);
    }

    private void confirmSetWinner(){
        final String msg = String.format(mContext.getString(R.string.set_winner_confirmation_msg),
                mScoreboardItem.getTeam().getName());
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(BaseActivity.getMessage(msg))
                .setTitle(R.string.app_name)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setWinner();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        builder.create().show();
    }

    private void setWinner(){
        Rally.getInstance().setTeamWinner(mScoreboardItem.getTeam());
        getActivity().mDatabase.child("games").child(String.valueOf(getActivity().
                mPreferencesManager.getGameId())).child("status").setValue(BaseActivity.STATUS_FINISHED);
        getActivity().mDatabase.child("games").child(String.valueOf(getActivity().
                mPreferencesManager.getGameId())).child("finishdate").setValue(System.currentTimeMillis());
    }

    private TeamActivity getActivity() {
        return ((TeamActivity) mContext);
    }

    public void setScoreboardItem(ScoreboardItem scoreboardItem, int position) {
        this.mScoreboardItem = scoreboardItem;
        this.mPosition = position;
        notifyChange();
    }
}

package com.easywaypop.app.rally.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.view.ChallengeActivity;
import com.easywaypop.app.rally.view.TeamActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Juan-Crawford on 15/11/2016.
 */

public class ItemChallengeViewModel extends BaseObservable {

    private Context mContext;
    private Challenge mChallenge;
    private List<DoneChallenge> mDoneChallengeList;

    public ItemChallengeViewModel(Context context, Challenge challenge,
                                  List<DoneChallenge> doneChallengeList) {
        this.mContext = context;
        this.mChallenge = challenge;
        this.mDoneChallengeList = doneChallengeList;
    }

    @Bindable
    public String getPictureChallenge() {
        return getChallengeUrlImage();
    }

    private DoneChallenge getDoneChallenge() {
        for (DoneChallenge item : mDoneChallengeList) {
            if (item.getGameid() == mChallenge.getGameid() && item.getChallengeid()
                    == Long.valueOf(mChallenge.getChallengeId())) {
                return item;
            }
        }
        return null;
    }

    private String getChallengeUrlImage(){
        DoneChallenge doneChallenge = getDoneChallenge();
        return doneChallenge != null ? doneChallenge.getResources().getUrl() : "";
    }

    @BindingAdapter("challengeImageUrl")
    public static void setChallengeImageUrl(ImageView imageView, String url) {
        downloadImageFromUrl(imageView, url);
    }

    public void onItemClick(View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!getChallengeUrlImage().isEmpty()){
                    ChallengeActivity.navigate(getActivity(), mChallenge, getDoneChallenge());
                }else{
                    getActivity().showToastMessage(mContext.getString(R.string.cannot_see_challenge_msg));
                }
            }
        }, 200);
    }

    private static void downloadImageFromUrl(ImageView ivImage, String url) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.bg_no_challenge_complete)
                .showImageOnFail(R.drawable.bg_no_challenge_complete)
                .showImageOnLoading(R.drawable.bg_no_challenge_complete).build();
        imageLoader.displayImage(url, ivImage, options);
    }

    private TeamActivity getActivity() {
        return ((TeamActivity) mContext);
    }

    public void setChallenge(Challenge challenge, List<DoneChallenge> doneChallengeList) {
        this.mChallenge = challenge;
        this.mDoneChallengeList = doneChallengeList;
        notifyChange();
    }
}

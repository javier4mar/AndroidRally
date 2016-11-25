package com.easywaypop.app.rally.view;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.databinding.ItemChallengeImageBinding;
import com.easywaypop.app.rally.model.Challenge;
import com.easywaypop.app.rally.model.DoneChallenge;
import com.easywaypop.app.rally.viewmodel.ItemChallengeViewModel;

import java.util.Collections;
import java.util.List;

/**
 * Created by Juan-Crawford on 15/11/2016.
 */

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeAdapterViewHolder> {

    private List<Challenge> mChallengeList;
    private List<DoneChallenge> mDoneChallengeList;

    public ChallengeAdapter() {
        this.mChallengeList = Collections.emptyList();
    }

    @Override
    public ChallengeAdapter.ChallengeAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemChallengeImageBinding itemChallengeImageBinding = DataBindingUtil.inflate(LayoutInflater
                .from(parent.getContext()), R.layout.item_challenge_image, parent, false);
        return new ChallengeAdapter.ChallengeAdapterViewHolder(itemChallengeImageBinding);
    }

    @Override
    public void onBindViewHolder(ChallengeAdapter.ChallengeAdapterViewHolder holder, int position) {
        holder.bindScores(mChallengeList.get(position), mDoneChallengeList);
    }

    @Override
    public int getItemCount() {
        return mChallengeList.size();
    }

    public void setChallengeList(List<Challenge> challengeList, List<DoneChallenge> doneChallengeList) {
        this.mChallengeList = challengeList;
        this.mDoneChallengeList = doneChallengeList;
        notifyDataSetChanged();
    }

    public static class ChallengeAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemChallengeImageBinding mItemChallengeImageBinding;

        public ChallengeAdapterViewHolder(ItemChallengeImageBinding itemChallengeImageBinding) {
            super(itemChallengeImageBinding.rlContainer);
            this.mItemChallengeImageBinding = itemChallengeImageBinding;
        }

        void bindScores(Challenge challenge, List<DoneChallenge> doneChallengeList) {
            if (mItemChallengeImageBinding.getViewModel() == null) {
                mItemChallengeImageBinding.setViewModel(new ItemChallengeViewModel(itemView.getContext(),
                        challenge, doneChallengeList));
            } else {
                mItemChallengeImageBinding.getViewModel().setChallenge(challenge, doneChallengeList);
            }
        }
    }
}
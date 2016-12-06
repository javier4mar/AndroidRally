package com.cbwmarketing.app.rally.view;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.databinding.ItemScoreboardBinding;
import com.cbwmarketing.app.rally.model.ScoreboardItem;
import com.cbwmarketing.app.rally.viewmodel.ItemScoreboardViewModel;

import java.util.Collections;
import java.util.List;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ScoreboardAdapterViewHolder> {

    private List<ScoreboardItem> mScoreboardItemList;

    public ScoreboardAdapter() {
        this.mScoreboardItemList = Collections.emptyList();
    }

    @Override
    public ScoreboardAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemScoreboardBinding itemScoreboardBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_scoreboard, parent, false);
        return new ScoreboardAdapterViewHolder(itemScoreboardBinding);
    }

    @Override
    public void onBindViewHolder(ScoreboardAdapterViewHolder holder, int position) {
        holder.bindScores(mScoreboardItemList.get(position), position);
    }

    @Override public int getItemCount() {
        return mScoreboardItemList.size();
    }

    public void setScoreboardItemList(List<ScoreboardItem> list) {
        this.mScoreboardItemList = list;
        notifyDataSetChanged();
    }

    public static class ScoreboardAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemScoreboardBinding mItemScoreboardBinding;

        public ScoreboardAdapterViewHolder(ItemScoreboardBinding itemScoreboardBinding) {
            super(itemScoreboardBinding.rlContainer);
            this.mItemScoreboardBinding = itemScoreboardBinding;
        }

        void bindScores(ScoreboardItem scoreboardItem, int position) {
            if (mItemScoreboardBinding.getViewModel() == null) {
                mItemScoreboardBinding.setViewModel(new ItemScoreboardViewModel(itemView.getContext(),
                        scoreboardItem, position));
            } else {
                mItemScoreboardBinding.getViewModel().setScoreboardItem(scoreboardItem, position);
            }
        }
    }
}

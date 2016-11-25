package com.easywaypop.app.rally.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.Rally;
import com.easywaypop.app.rally.databinding.FragmentScoreboardBinding;
import com.easywaypop.app.rally.model.ScoreboardItem;
import com.easywaypop.app.rally.viewmodel.MainViewModelContract;
import com.easywaypop.app.rally.viewmodel.ScoreboardViewModel;

import java.util.List;

/**
 * Created by jcrawford on 10/11/2016.
 */

public class ScoreboardFragment extends Fragment implements MainViewModelContract.MainView {

    private TeamActivity mActivity;
    private ScoreboardViewModel mScoreboardViewModel;
    private ScoreboardAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (TeamActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentScoreboardBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_scoreboard, container, false);
        mScoreboardViewModel = new ScoreboardViewModel(this, mActivity);
        binding.setViewModel(mScoreboardViewModel);
        setupRecyclerView(binding.rvScores);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.getBinding().collapsingToolbar.setTitle(Rally.getInstance().getTeamName());
        mActivity.getBinding().collapsingToolbar.setTitleEnabled(true);
    }

    @Override
    public Fragment getFragment() {
        return ScoreboardFragment.this;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        mAdapter = new ScoreboardAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScoreboardViewModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScoreboardViewModel.onPause();
    }

    public void loadImage(String url) {
        BaseActivity.loadImage(mActivity, url, mActivity.getBinding().ivCover);
    }

    public void loadScoreboard(List<ScoreboardItem> list) {
        mAdapter.setScoreboardItemList(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.getBinding().ivCover.setImageResource(R.drawable.bg_fake);
        mScoreboardViewModel.destroy();
    }
}

package com.cbwmarketing.app.rally.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.databinding.FragmentTeamBinding;
import com.cbwmarketing.app.rally.model.ScoreboardItem;
import com.cbwmarketing.app.rally.viewmodel.MainViewModelContract;
import com.cbwmarketing.app.rally.viewmodel.TeamViewModel;


/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class TeamFragment extends Fragment implements MainViewModelContract.MainView {

    private static final String ARG_TEAM_ID = "teamId";
    private long mTeamId;
    private TeamActivity mActivity;
    private TeamViewModel mTeamViewModel;
    private ChallengeAdapter mAdapter;

    public static TeamFragment newInstance(long teamId) {
        TeamFragment f = new TeamFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_TEAM_ID, teamId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (TeamActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTeamId = getArguments().getLong(ARG_TEAM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTeamBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_team, container, false);
        mTeamViewModel = new TeamViewModel(this, mActivity, mTeamId);
        binding.setViewModel(mTeamViewModel);
        setupRecyclerView(binding.rvChallenges);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.getBinding().collapsingToolbar.setTitleEnabled(false);
    }

    @Override
    public Fragment getFragment() {
        return TeamFragment.this;
    }

    public void initViews(ScoreboardItem scoreboardItem) {
        if (scoreboardItem != null) {
            loadTeamImage(scoreboardItem.getTeam().getTeamimage());
            populateTeamData(scoreboardItem);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        mAdapter = new ChallengeAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    public void loadTeamImage(String url) {
        BaseActivity.loadImage(mActivity, url, mActivity.getBinding().ivCover);
    }

    public void populateTeamData(ScoreboardItem scoreboardItem) {
        if(scoreboardItem != null) {
            mActivity.setToolbarTitle(scoreboardItem.getTeam().getName());
            mActivity.getBinding().tvTitle.setText(scoreboardItem.getTeam().getName());
            mActivity.getBinding().tvWins.setText(BaseActivity.getMessage(String.
                    format(getString(R.string.wins_format), scoreboardItem.getDoneChallengeList().size())));
            final int left = scoreboardItem.getChallengeList().size() -
                    scoreboardItem.getDoneChallengeList().size();
            mActivity.getBinding().tvReminder.setText(BaseActivity.getMessage(String.
                    format(getString(R.string.left_format), left > 0 ? left : left * -1)));
            mAdapter.setChallengeList(scoreboardItem.getChallengeList(), scoreboardItem.getDoneChallengeList());
            mTeamViewModel.hideProgress();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mTeamViewModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mTeamViewModel.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearHeader();
        mActivity.getBinding().ivCover.setImageResource(R.drawable.bg_fake);
        mTeamViewModel.destroy();
    }

    private void clearHeader(){
        mActivity.getBinding().tvTitle.setText("");
        mActivity.getBinding().tvWins.setText("");
        mActivity.getBinding().tvReminder.setText("");
    }
}

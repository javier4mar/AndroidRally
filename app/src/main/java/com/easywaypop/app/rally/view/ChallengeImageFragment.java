package com.easywaypop.app.rally.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.databinding.FragmentChallengeImageBinding;

/**
 * Created by Juan-Crawford on 9/11/2016.
 */

public class ChallengeImageFragment extends Fragment {

    private static final String ARG_IMAGE_DATA = "imageData";
    private String mImage;
    private FragmentActivity mActivity;

    public static ChallengeImageFragment newInstance(String imageData) {
        ChallengeImageFragment f = new ChallengeImageFragment();
        Bundle b = new Bundle();
        b.putString(ARG_IMAGE_DATA, imageData);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mImage = getArguments().getString(ARG_IMAGE_DATA, "");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentChallengeImageBinding binding = DataBindingUtil.
                inflate(inflater, R.layout.fragment_challenge_image, container, false);
        BaseActivity.loadImage(mActivity, mImage, binding.ivImage);
        return binding.getRoot();
    }
}

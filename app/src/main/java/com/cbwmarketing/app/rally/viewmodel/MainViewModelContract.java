package com.cbwmarketing.app.rally.viewmodel;

import android.support.v4.app.Fragment;

/**
 * Created by jcrawford on 8/11/2016.
 */

public interface MainViewModelContract {

    interface MainView {
        Fragment getFragment();
    }

    interface ViewModel extends com.cbwmarketing.app.rally.viewmodel.ViewModel {
        void onResume();

        void onPause();
    }
}

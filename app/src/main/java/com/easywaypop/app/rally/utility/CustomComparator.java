package com.easywaypop.app.rally.utility;

import com.easywaypop.app.rally.model.ScoreboardItem;

import java.util.Comparator;

/**
 * Created by Juan-Crawford on 12/11/2016.
 */

public class CustomComparator implements Comparator<ScoreboardItem> {
    @Override
    public int compare(ScoreboardItem o1, ScoreboardItem o2) {
        return String.valueOf(o2.getDoneChallengeList().size())
                .compareTo(String.valueOf(o1.getDoneChallengeList().size()));
    }
}

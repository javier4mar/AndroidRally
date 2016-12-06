package com.cbwmarketing.app.rally.utility;

import android.content.Context;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

/**
 * Created by jcrawford on 8/22/2016.
 */
public class DrawerArrowAnimation {

    public static class DrawerArrowDrawableToggle extends DrawerArrowDrawable implements DrawerToggle {

        public DrawerArrowDrawableToggle(Context themedContext) {
            super(themedContext);
        }

        public void setPosition(float position) {
            if (position == 1f) {
                setVerticalMirror(true);
            } else if (position == 0f) {
                setVerticalMirror(false);
            }
            setProgress(position);
        }

        public float getPosition() {
            return getProgress();
        }
    }

    public interface DrawerToggle {

        void setPosition(float position);

        float getPosition();
    }
}

package com.easywaypop.app.rally.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Juan-Crawford on 5/11/2016.
 */

public class ChallengeTextView extends TextView {

    private Context mContext;

    public ChallengeTextView(Context context) {
        super(context);
        this.mContext = context;
    }

    public ChallengeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int toolbarHeight = getToolbarHeight(mContext) + getStatusBarHeight(mContext);
        Point size = getScreenDimensions(mContext);
        int height = size.y - toolbarHeight;
        setMeasuredDimension(widthMeasureSpec, height);
    }

    private static int getToolbarHeight(Context context){
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        return 0;
    }

    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) return context.getResources().getDimensionPixelSize(resourceId);
        return 0;
    }

    private static Point getScreenDimensions(Context context) {
        Point size = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);
        return size;
    }
}

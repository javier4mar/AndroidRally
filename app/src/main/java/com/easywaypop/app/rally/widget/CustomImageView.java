package com.easywaypop.app.rally.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

    Context mContext;

    public CustomImageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
                getResources().getDisplayMetrics());
        if(getMeasuredHeight() > 0) {
            int width = (getMeasuredWidth() * height) / getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
    }
}

package com.easywaypop.app.rally.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.widget.ImageView;

import com.easywaypop.app.rally.R;

import java.lang.ref.WeakReference;

public class DownloadPhotoAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> ivPhotoReference;
    private Context mContext;

    public DownloadPhotoAsyncTask(Context context, ImageView ivPhoto) {
        this.ivPhotoReference = new WeakReference<>(ivPhoto);
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        ImageView ivPhoto = this.ivPhotoReference.get();
        if (ivPhoto != null)
            ivPhoto.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_fake));
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap;
        if (!params[0].isEmpty() && !params[0].startsWith("res")) {
            byte[] b = Base64.decode(params[0].substring(params[0].indexOf(",") + 1), Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        } else
            bitmap = null;
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView ivPhoto = this.ivPhotoReference.get();
        if (ivPhoto != null) {
            if (bitmap != null)
                ivPhoto.setImageBitmap(bitmap);
            else
                ivPhoto.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_fake));
        }
    }
}

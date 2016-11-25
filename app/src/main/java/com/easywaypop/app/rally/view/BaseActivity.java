package com.easywaypop.app.rally.view;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.model.Game;
import com.easywaypop.app.rally.model.Resource;
import com.easywaypop.app.rally.utility.DownloadPhotoAsyncTask;
import com.easywaypop.app.rally.utility.PreferencesManager;
import com.easywaypop.app.rally.utility.ScalingUtilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Created by jcrawford on 16/9/2016.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String STORAGE_REFERENCE = "gs://rally-app-easywaypop.appspot.com";
    public static final String FILE_PROVIDER_AUTHORITY = "com.easywaypop.app.rally.provider";
    public static final String PACKAGE_NAME = "com.easywaypop.app.rally";
    public static final String ADMIN_ROL = "moderator";
    public static final String LOGIN_IMAGE = "login";
    public static final String SPLASH_IMAGE = "splash";
    public static final String ID_NOTIFICATION = "pIdNotification";
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_FINISHED = 0;
    private static final String FORMAT = "%02d:%02d:%02d\"";
    public FirebaseAuth mFirebaseAuth;
    public FirebaseUser mFirebaseUser;
    public DatabaseReference mDatabase;
    public StorageReference mStorageRef;
    public PreferencesManager mPreferencesManager;
    private static final String EMPTY_STRING = "";
    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferencesManager = new PreferencesManager(getApplicationContext());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(STORAGE_REFERENCE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private static boolean isVersionN(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isNullOrEmpty(final String string) {
        return string == null || EMPTY_STRING.equals(string);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.
                getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v != null)
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static String formatHour(long timeStamp){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", getLocale());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return formatter.format(calendar.getTime());
    }

    public boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public String getUsrEmail(String usr){
        return String.format("%s@laica.co.cr", usr);
    }

    public void showGeneralAlertDialog(String msg){
        if(mDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
            builder.setMessage(msg)
                    .setTitle(R.string.app_name)
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            mDialog = null;
                        }
                    });
            mDialog = builder.create();
            mDialog.show();
        }
    }

    public void showGeneralAlertDialog(String msg, DialogInterface.OnClickListener onClickListener){
        if(mDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
            builder.setMessage(getMessage(msg))
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, onClickListener)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            mDialog = null;
                        }
                    });
            mDialog = builder.create();
            mDialog.show();
        }
    }

    public void showToastMessage(String msg){
        Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public static String formatChronometer(long milliseconds) {
        return String.format(getLocale(), FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public static Locale getLocale(){
        return Locale.getDefault();
    }

    public static boolean haveNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation_label))
                .setMessage(getString(R.string.logout_confirmation_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPreferencesManager.logoutUser();
                        mFirebaseAuth.signOut();
                        goToLoginActivity(BaseActivity.this);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    public void launchActivity(Activity activity, Class<?> newActivity, boolean isOverride){
        Intent intent = new Intent(activity, newActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        if(isOverride) activity.overridePendingTransition(R.anim.activity_slide_in_left,
                R.anim.activity_slide_out_left);
        activity.finish();
    }

    public void goToLoginActivity(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        BaseActivity.this.startActivity(intent);
        ActivityCompat.finishAffinity(activity);
    }

    @SuppressWarnings("deprecation")
    public static Spanned getMessage(String htmlMsg){
        if(isVersionN())
            return Html.fromHtml(htmlMsg, Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(htmlMsg);
    }

    public static boolean isPermissonsGranted(int[] grantResults) {
        boolean flag = true;
        for (int item : grantResults) {
            if (item != PackageManager.PERMISSION_GRANTED) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public long getCurrentTime(Game game) {
        if (game != null) {
            long gameTime = TimeUnit.MINUTES.toMillis(game.getTimeinminutes());
            long startTime = game.getStartdate();
            long remaninderTime = (System.currentTimeMillis() - (startTime + gameTime));
            return remaninderTime > 0 ? remaninderTime : remaninderTime * -1;
        }
        return 0;
    }

    public static void loadImage(Context context, String url, ImageView imageView){
        if (!url.contains("http")) {
            new DownloadPhotoAsyncTask(context, imageView).execute(url);
        } else {
            BaseActivity.downloadImageFromUrl(imageView, url);
        }
    }

    public static void downloadImageFromUrl(ImageView ivImage, String url) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(R.drawable.bg_fake)
                .showImageOnFail(R.drawable.bg_fake)
                .showImageOnLoading(R.drawable.bg_fake).build();
        imageLoader.displayImage(url, ivImage, options);
    }

    public boolean isAdmin(){
        return mPreferencesManager.getUserRol().equalsIgnoreCase(ADMIN_ROL);
    }

    public static int getBackgroundColor(View view) {
        Drawable drawable = view.getBackground();
        if (drawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            if (Build.VERSION.SDK_INT >= 11) {
                return colorDrawable.getColor();
            }
            try {
                Field field = colorDrawable.getClass().getDeclaredField("mState");
                field.setAccessible(true);
                Object object = field.get(colorDrawable);
                field = object.getClass().getDeclaredField("mUseColor");
                field.setAccessible(true);
                return field.getInt(object);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ignored) {
            }
        }
        return 0;
    }

    public static void createNotification(Context context, String msg, int notId, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        intent.putExtra(BaseActivity.ID_NOTIFICATION, notId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setWhen(System.currentTimeMillis());
        notificationBuilder.setContentTitle(context.getString(R.string.app_name));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setLights(Color.BLUE, 1500, 3000);
        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        notificationBuilder.setContentText(msg);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notId, notificationBuilder.build());
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            final String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }catch (Exception e){
            return new File (contentUri.toString()).getAbsolutePath();
        }
    }

    public static String decodeFileDescriptor(Context context, Uri uri, FileDescriptor fd, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String path = getRealPathFromUri(context, uri);
        String strMyImagePath = null;
        Bitmap scaledBitmap;
        try {
            Bitmap unscaledBitmap = ScalingUtilities.decodeFileDescriptor(fd, DESIREDWIDTH, DESIREDHEIGHT,
                    ScalingUtilities.ScalingLogic.FIT);
            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT,
                        ScalingUtilities.ScalingLogic.FIT);
                scaledBitmap = rotateImageIfRequired(scaledBitmap, uri);
            } else {
                unscaledBitmap.recycle();
                return path;
            }
            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMPRALLY");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }
            final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", getLocale()).format(new Date());
            String s = String.format("IMG_%s.png",timeStamp);
            File f = new File(mFolder.getAbsolutePath(), s);
            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            scaledBitmap.recycle();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;
    }

    public static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static boolean deleteTempFile(String fileName){
        String extr = Environment.getExternalStorageDirectory().toString();
        File archivoMalo = new File(extr + "/TMPRALLY/" + fileName);
        return archivoMalo.exists() && archivoMalo.delete();
    }

    public static void refreshGalleryData(Context context, String fileName) {
        if (BaseActivity.isKitkat()) {
            String extr = Environment.getExternalStorageDirectory().toString();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(new File(extr + "/TMPRALLY/" + fileName)));
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    public static String fileExt(String url) {
        if (url.contains("?"))
            url = url.substring(0, url.indexOf("?"));
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.contains("%"))
                ext = ext.substring(0, ext.indexOf("%"));
            if (ext.contains("/"))
                ext = ext.substring(0, ext.indexOf("/"));
            return ext.toLowerCase();
        }
    }

    public void getCompanyImage(final OnLoadCompanyImageListener listener) {
        mDatabase.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Game game = postSnapshot.getValue(Game.class);
                    if (game.getGameid() == mPreferencesManager.getGameId() && listener != null)
                        listener.onLoadImage(game.getResources());
                }
                mDatabase.child("games").removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mDatabase.child("games").removeEventListener(this);
            }
        });
    }

    public interface OnLoadCompanyImageListener {
        void onLoadImage(List<Resource> resources);
    }
}

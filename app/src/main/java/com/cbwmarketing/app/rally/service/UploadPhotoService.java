package com.cbwmarketing.app.rally.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.Rally;
import com.cbwmarketing.app.rally.model.Challenge;
import com.cbwmarketing.app.rally.utility.PreferencesManager;
import com.cbwmarketing.app.rally.view.BaseActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

public class UploadPhotoService extends Service {

    public static final String SERVICE_ACTION = "com.cbwmarketing.app.rally.service.UploadPhotoService.SERVICE";
    public static final String EXTRA_CHALLENGE_STARTED = "extra-challenge-started";
    public static final String EXTRA_CHALLENGE_SELECTED = "extra-challenge-selected";
    public static final String EXTRA_CHALLENGE_PHOTO = "extra-challenge-photo";
    private static final int NOTIFICATION_ID = 18;
    private long mChallengeStarted;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private PreferencesManager mPreferencesManager;
    private Challenge mSelectedChallenge;
    private Context mContext;

    public UploadPhotoService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = UploadPhotoService.this;
        mPreferencesManager = new PreferencesManager(mContext);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(BaseActivity.STORAGE_REFERENCE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doServiceStart(intent);
        return Service.START_REDELIVER_INTENT;
    }

    private void doServiceStart(Intent intent) {
        mChallengeStarted = intent.getLongExtra(EXTRA_CHALLENGE_STARTED, 0);
        mSelectedChallenge = intent.getParcelableExtra(EXTRA_CHALLENGE_SELECTED);
        Uri photoUri = intent.getParcelableExtra(EXTRA_CHALLENGE_PHOTO);
        uploadPhoto(photoUri);
    }

    public void uploadPhoto(Uri photoUri) {
        try {
            if (photoUri != null) {
                int MAXSIZE = 300;
                int DESIREDWIDTH = 1024;
                int DESIREDHEIGHT, imageHeight, imageWidth;
                long fileSize = new File(photoUri.getPath()).length();
                BitmapFactory.Options options = new BitmapFactory.Options();
                ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(photoUri, "r");
                if (fd != null) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                    imageHeight = options.outHeight;
                    imageWidth = options.outWidth;
                    if (imageWidth > DESIREDWIDTH || fileSize > MAXSIZE) {
                        DESIREDHEIGHT = (imageHeight * DESIREDWIDTH) / imageWidth;
                        sendPhotoToFirebase(Uri.fromFile(new File(BaseActivity.decodeFileDescriptor(mContext,
                                photoUri, fd.getFileDescriptor(), DESIREDWIDTH, DESIREDHEIGHT))), true);
                    } else {
                        sendPhotoToFirebase(photoUri, false);
                    }
                    fd.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPhotoToFirebase(Uri photoUri, final boolean isDelete) {
        if (BaseActivity.haveNetworkConnection(mContext)) {
            final String _fileName = photoUri.getLastPathSegment();
            final String _extension = BaseActivity.fileExt(_fileName);
            final String _fileExt = _extension != null ? _extension.replace(".", "") : "";
            final StorageMetadata _metadata = new StorageMetadata.Builder()
                    .setContentType(String.format("image/%s", _fileExt))
                    .build();
            final StorageReference _imageRef = mStorageRef.child(String.
                    format("images/%s", _fileName));
            createUploadNotification();
            final UploadTask _uploadTask = _imageRef.putFile(photoUri, _metadata);
            _uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    updateUploadNotificationProgress((int) progress);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showAlertDialog(mContext.getString(R.string.upload_image_failure_msg));
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (taskSnapshot != null && taskSnapshot.getMetadata() != null) {
                        final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        if (downloadUrl != null) {
                            insertDownloadUrl(downloadUrl);
                            if (isDelete && BaseActivity.deleteTempFile(downloadUrl.getLastPathSegment()))
                                BaseActivity.refreshGalleryData(mContext, downloadUrl.getLastPathSegment());
                        }
                    }
                }
            });
        } else
            showToastMessage(mContext.getString(R.string.no_connected_msg));
    }

    private void insertDownloadUrl(Uri downloadUrl) {
        if(mSelectedChallenge != null) {
            final long finishTime = System.currentTimeMillis();
            HashMap<String, Object> _result = new HashMap<>();
            _result.put("challengeid", Long.valueOf(mSelectedChallenge.getChallengeId()));
            _result.put("datefinish", finishTime);
            _result.put("datestarted", mChallengeStarted);
            _result.put("gameid", mPreferencesManager.getGameId());
            _result.put("latitude", mSelectedChallenge.getLatitude());
            _result.put("longitude", mSelectedChallenge.getLongitude());
            _result.put("resources", getResource(downloadUrl, finishTime));
            _result.put("status", BaseActivity.STATUS_ACTIVE);
            _result.put("teamid", mPreferencesManager.getTeamId());
            mDatabase.child("donechallenges").push().setValue(_result);
            Rally.getInstance().setChallengeOpen(false);
            finishUploadNotification();
        }
    }

    private HashMap<String, Object> getResource(Uri url, long createDate){
        HashMap<String, Object> resource = new HashMap<>();
        resource.put("createddate", createDate);
        resource.put("name", url.getLastPathSegment());
        resource.put("type", "image");
        resource.put("url", url.toString());
        return resource;
    }

    private void createUploadNotification(){
        showToastMessage(mContext.getString(R.string.init_upload_image_progress_msg));
        mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = new NotificationCompat.Builder(mContext);
        mNotification.setContentTitle(mContext.getString(R.string.app_name));
        mNotification.setContentText(mContext.getString(R.string.upload_in_progress_msg));
        mNotification.setSmallIcon(R.mipmap.ic_upload_white_24dp);
        mNotification.setProgress(0, 0, true);
        mNotification.setOngoing(true);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    private void updateUploadNotificationProgress(int progress){
        mNotification.setProgress(100, progress, false);
        mNotification.setNumber(progress);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    private void finishUploadNotification() {
        mNotification.setProgress(0, 0, false);
        mNotification.setOngoing(false);
        mNotification.setAutoCancel(true);
        mNotification.setNumber(0);
        mNotification.setContentText(mContext.getString(R.string.upload_complete_msg));
        mNotification.setPriority(Notification.PRIORITY_HIGH);
        mNotification.setDefaults(Notification.DEFAULT_VIBRATE);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
                UploadPhotoService.this.stopSelf();
            }
        }, 200);
    }

    public void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg)
                .setTitle(R.string.app_name)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    public void showToastMessage(String msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        mNotificationManager = null;
        mNotification = null;
        mDatabase = null;
        mStorageRef = null;
        mPreferencesManager = null;
        mSelectedChallenge = null;
        mContext = null;
        super.onDestroy();
    }
}

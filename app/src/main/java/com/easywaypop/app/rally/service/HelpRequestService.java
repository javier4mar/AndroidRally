package com.easywaypop.app.rally.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.easywaypop.app.rally.R;
import com.easywaypop.app.rally.model.HelpMarker;
import com.easywaypop.app.rally.model.HelpRequest;
import com.easywaypop.app.rally.model.Team;
import com.easywaypop.app.rally.model.User;
import com.easywaypop.app.rally.utility.PreferencesManager;
import com.easywaypop.app.rally.view.BaseActivity;
import com.easywaypop.app.rally.view.MainActivity;
import com.easywaypop.app.rally.view.MapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HelpRequestService extends Service implements ValueEventListener {

    public static final String SERVICE_ACTION = "com.easywaypop.app.rally.service.HelpRequestService.SERVICE";
    public static final String EXTRA_HELP_MARKER_DATA = "extra-help-marker-data";
    private PreferencesManager mPreferencesManager;
    private DatabaseReference mDatabase;
    private Context mContext;

    public HelpRequestService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = HelpRequestService.this;
        mPreferencesManager = new PreferencesManager(mContext);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerHelpRequestListener();
        return Service.START_REDELIVER_INTENT;
    }

    public void registerHelpRequestListener() {
        mDatabase.child("helprequest").addValueEventListener(this);
    }

    private void unRegisterHelpRequestListener() {
        mDatabase.child("helprequest").removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            final HelpRequest helpRequest = postSnapshot.getValue(HelpRequest.class);
            if (helpRequest.getGameid() == mPreferencesManager.getGameId() && helpRequest.getStatus()
                    != BaseActivity.STATUS_FINISHED && mContext != null) {
                final HelpMarker helpMarker = new HelpMarker();
                helpRequest.setRequestId(postSnapshot.getKey());
                helpMarker.setHelpRequest(helpRequest);
                getUserInfo(helpMarker, helpRequest);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        unRegisterHelpRequestListener();
    }

    private void getUserInfo(final HelpMarker helpMarker, final HelpRequest helpRequest) {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final User user = postSnapshot.getValue(User.class);
                    if (user.getFirebaseuid().equals(helpRequest.getFirebaseuid()) && mContext != null) {
                        helpMarker.setUser(user);
                        getUserTeamInfo(helpMarker, user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getUserTeamInfo(final HelpMarker helpMarker, final User user) {
        mDatabase.child("teams").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Team team = postSnapshot.getValue(Team.class);
                    if (team.getTeamid() == user.getTeamid() && team.getGameid() == user.getGameid()
                            && mContext != null) {
                        helpMarker.setTeam(team);
                        sendBroadcastToMapFragment(helpMarker);
                        if (helpMarker.getHelpRequest().getStatus() == BaseActivity.STATUS_ACTIVE) {
                            sendHelpNotification(helpMarker);
                            mDatabase.child("helprequest").child(helpMarker.getHelpRequest().
                                    getRequestId()).child("status").setValue(BaseActivity.STATUS_RUNNING);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void sendHelpNotification(final HelpMarker request) {
        final String msg = String.format(mContext.getString(R.string.help_notification_msg),
                request.getUser().getName(), request.getUser().getLastname(), request.getTeam().getName());
        BaseActivity.createNotification(mContext, msg, BaseActivity.STATUS_ACTIVE, MainActivity.class);
    }

    private void sendBroadcastToMapFragment(HelpMarker helpMarker) {
        Intent intent = new Intent(MapFragment.GET_HELP_REQUEST_BROADCAST);
        intent.putExtra(EXTRA_HELP_MARKER_DATA, helpMarker);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        unRegisterHelpRequestListener();
        mDatabase = null;
        mPreferencesManager = null;
        mContext = null;
        super.onDestroy();
    }
}

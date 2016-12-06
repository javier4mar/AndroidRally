package com.cbwmarketing.app.rally.view;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cbwmarketing.app.rally.R;
import com.cbwmarketing.app.rally.Rally;
import com.cbwmarketing.app.rally.databinding.FragmentMapBinding;
import com.cbwmarketing.app.rally.model.Challenge;
import com.cbwmarketing.app.rally.model.DoneChallenge;
import com.cbwmarketing.app.rally.model.HelpMarker;
import com.cbwmarketing.app.rally.model.Image;
import com.cbwmarketing.app.rally.service.HelpRequestService;
import com.cbwmarketing.app.rally.utility.ChallengeImagesPagerAdapter;
import com.cbwmarketing.app.rally.viewmodel.MainViewModelContract;
import com.cbwmarketing.app.rally.viewmodel.MapViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcrawford on 3/11/2016.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener, MainViewModelContract.MainView {

    public static final String GET_HELP_REQUEST_BROADCAST = "com.cbwmarketing.app.rally.getHelpRequest";
    protected static final int REQUEST_CHECK_SETTINGS = 0x2;
    private static final int LOCATION_PERMISSIONS_REQUEST = 5;
    private static final int CAMERA_PERMISSIONS_REQUEST = 6;
    private static final int REQUEST_IMAGE_CAPTURE = 15;
    protected GoogleApiClient mGoogleApiClient;
    private boolean isFabClicked = false, isMarkersLoaded = false;
    private String mImagePath;
    private MainActivity mActivity;
    private MapViewModel mMapViewModel;
    private Challenge mChallenge;
    private List<Challenge> mChallengeList;
    private List<DoneChallenge> mDoneChallengeList;
    private LocationRequest mLocationRequest;
    private TextView tvTitle, tvWins, tvLeft, tvInstructions;
    private View llButtons;
    private GoogleMap mMap;
    private LocalBroadcastManager mLocalBroadcastManager;

    private BroadcastReceiver mGetHelpRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final HelpMarker helpMarker = intent.getParcelableExtra(HelpRequestService.
                    EXTRA_HELP_MARKER_DATA);
            setHelpRequestMarker(helpMarker);
        }
    };





    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity.getBinding().toolbar.setTitleTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimaryText));
        FragmentMapBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_map, container, false);
        mMapViewModel = new MapViewModel(this, mActivity);
        binding.setViewModel(mMapViewModel);
        findViews();
        showOrHideButtons(!mActivity.isAdmin());
        initilizeMap();
        registerBroadcastReceiver();
        return binding.getRoot();
    }

    @Override
    public MapFragment getFragment() {
        return MapFragment.this;
    }

    private void findViews(){
        tvTitle = (TextView) mActivity.findViewById(R.id.tvBSTitle);
        tvWins = (TextView) mActivity.findViewById(R.id.tvBSWins);
        tvLeft = (TextView) mActivity.findViewById(R.id.tvBSReminder);
        tvInstructions = (TextView) mActivity.findViewById(R.id.tvInstructions);
        llButtons = mActivity.findViewById(R.id.llButtons);
        mActivity.findViewById(R.id.llCamera).setOnClickListener(this);
        mActivity.findViewById(R.id.llMap).setOnClickListener(this);
    }

    private void initilizeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        buildGoogleApiClient();
        mActivity.getBinding().fab.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (BaseActivity.isPermissonsGranted(grantResults)) {
            switch (requestCode) {
                case LOCATION_PERMISSIONS_REQUEST:
                    if (grantResults.length == 1) setUpMap();
                    else buildGoogleApiClient();
                    break;
                case CAMERA_PERMISSIONS_REQUEST:
                    createCameraIntent();
                    break;
            }
        } else {
            mActivity.showToastMessage(mActivity.getString(R.string.permissions_error_msg));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab:
                isFabClicked = true;
                if (mGoogleApiClient != null) getMyLocation();
                else buildGoogleApiClient();
                break;
            case R.id.llCamera:
                createCameraIntent();
                break;
            case R.id.llMap:
                hideMissings();
                mActivity.setBottomSheetCollapsed();
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mActivity.hideBottomSheet();
    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
        } else if (mMap != null) {
            mMap.clear();
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapClickListener(this);
            if (!isMarkersLoaded) loadChallengesMarkers();
        }
    }

    private void moveCameraMap(double latitude, double longitude, boolean isWithAnimation) {
        if (mMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(18)
                    .bearing(300F) // orientation
                    .tilt(50F) // viewing angle
                    .build();
            if (isWithAnimation)
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            else
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private CircleOptions createCircleRadius(LatLng latLng){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(20);
        circleOptions.strokeColor(Color.TRANSPARENT);
        circleOptions.fillColor(Color.TRANSPARENT);
        mMap.addCircle(circleOptions);
        return circleOptions;
    }

    public void loadChallengesMarkers() {
        if (mMap != null && mChallengeList != null && !isMarkersLoaded) {
            mMap.clear();
            for (Challenge item : mChallengeList) {
                LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
                if (item.getStatus() == 2)
                    mMap.addMarker(new MarkerOptions().position(latLng).title(item.getTitle())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_extra_challenge)));
                else
                    mMap.addMarker(new MarkerOptions().position(latLng).title(item.getTitle())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)));
                item.setRadius(createCircleRadius(latLng));
                mMap.setOnMarkerClickListener(this);
            }
            isMarkersLoaded = true;
        } else
            isMarkersLoaded = false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mChallenge = mMapViewModel.getChallenge(marker);
        if (mChallenge != null) {
            if (!mActivity.isAdmin() && (mActivity.getReminderTime() > 0) && mMapViewModel.validateRadius(mChallenge.getRadius())){
                mMapViewModel.acceptChallenge(mChallenge);
                Rally.getInstance().setChallengeOpen(true);
            }else
                mMapViewModel.showBottomSheet(200);
        } else if (mActivity.isAdmin()) {
            mMapViewModel.finishHelRequest(marker);
        }
        return false;
    }

    private void showOrHideButtons(boolean flag) {
        if (flag) llButtons.setVisibility(View.VISIBLE);
        else llButtons.setVisibility(View.GONE);
    }

    private void showMissings(){
        tvLeft.setVisibility(View.VISIBLE);
    }

    private void hideMissings(){
        tvLeft.setVisibility(View.INVISIBLE);
    }

    public void setInfoInBottomSheetWhenCollapsed(){
        if(mChallenge != null) {
            mActivity.changeTitleOfBottomSheet(mChallenge.getTitle());
            hideMissings();
            tvTitle.setText(mChallenge.getTitle());
            tvWins.setText(mChallenge.getBody());
            tvInstructions.setText(BaseActivity.getMessage(mChallenge.getBody()));
            tvInstructions.setMovementMethod(new LinkMovementMethod());
            Linkify.addLinks(tvInstructions, Patterns.WEB_URL, "http://");
            initViewpager(mChallenge.getImages());
        }
    }

    public void setInfoInBottomSheetWhenAnchorPoint() {
        if (mChallenge != null && !mActivity.isAdmin()) {
            showMissings();
            tvWins.setText(BaseActivity.getMessage(String.format(getString(R.string.wins_format),
                    getDoneChallengeList().size())));
            tvLeft.setText(BaseActivity.getMessage(String.format(getString(R.string.left_format),
                    mChallengeList.size())));
        }
    }

    private void initViewpager(List<Image> images){
        final ViewPager viewpager = mActivity.getBinding().pager;
        viewpager.setAdapter(new ChallengeImagesPagerAdapter(mActivity.getSupportFragmentManager(), images));
        mActivity.getBinding().indicator.setViewPager(viewpager);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        getMyLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.showErrorDialogFragment(result.getErrorCode(), mActivity,
                    MapFragment.this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            moveCameraMap(location.getLatitude(), location.getLongitude(), isFabClicked);
            isFabClicked = false;
            stopLocationUpdates();
            disconnectLocationApi();
        }
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                mMap.setMyLocationEnabled(true);
                moveCameraMap(lastLocation.getLatitude(), lastLocation.getLongitude(), isFabClicked);
                isFabClicked = false;
                disconnectLocationApi();
            } else
                showSettingsRequest();
        } else {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
        }
    }

    private void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(30 * 1000);
            mLocationRequest.setFastestInterval(5 * 1000);
        }
    }

    public void showSettingsRequest() {
        createLocationRequest();
        if (mGoogleApiClient == null) buildGoogleApiClient();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                            disconnectLocationApi();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapViewModel.onResume();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        isMarkersLoaded = false;
        mMapViewModel.onPause();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        if (!Rally.getInstance().isChallengeOpen())
            mActivity.hideBottomSheet();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegisterBroadcastReceiver();
        mMapViewModel.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        buildGoogleApiClient();
                        break;
                    case Activity.RESULT_CANCELED:
                        mActivity.showGeneralAlertDialog(getString(R.string.need_gps_error_msg));
                        break;
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        mMapViewModel.uploadPhoto(Uri.parse(mImagePath));
                        break;
                    case Activity.RESULT_CANCELED:
                        mActivity.showGeneralAlertDialog(mActivity.
                                getString(R.string.need_take_picture_to_challenge_msg));
                        break;
                }
                break;
        }
    }

    private void setHelpRequestMarker(HelpMarker requestMarker) {
        if (requestMarker != null) {
            mMapViewModel.addHelpMarker(requestMarker);
            LatLng latLng = new LatLng(requestMarker.getHelpRequest().getLatitude(),
                    requestMarker.getHelpRequest().getLongitude());
            final String title = String.format("%s %s", requestMarker.getUser().getName(),
                    requestMarker.getUser().getLastname());
            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(title)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_sos_marker)));
            marker.showInfoWindow();
        }
    }

    private void createCameraIntent() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            try {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imageFile = mActivity.createImageFile();
                mImagePath = String.format("file:%s", imageFile.getAbsolutePath());
                final Uri photoUri = FileProvider.getUriForFile(mActivity, BaseActivity.
                        FILE_PROVIDER_AUTHORITY, imageFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                if (takePicture.resolveActivity(getActivity().getPackageManager()) != null)
                    startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST);
        }
    }

    private void registerBroadcastReceiver(){
        mLocalBroadcastManager.registerReceiver(mGetHelpRequestReceiver,
                new IntentFilter(GET_HELP_REQUEST_BROADCAST));
    }

    private void unRegisterBroadcastReceiver(){
        mLocalBroadcastManager.unregisterReceiver(mGetHelpRequestReceiver);
    }

    private void disconnectLocationApi() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    protected void startLocationUpdates() throws SecurityException {
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
    }

    public void setMarkersLoaded(boolean markersLoaded) {
        isMarkersLoaded = markersLoaded;
    }

    public List<Challenge> getChallengeList() {
        if (mChallengeList == null) mChallengeList = new ArrayList<>();
        return mChallengeList;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.mChallengeList = challenges;
    }

    private List<DoneChallenge> getDoneChallengeList() {
        if (mDoneChallengeList == null) mDoneChallengeList = new ArrayList<>();
        return mDoneChallengeList;
    }

    public void setDoneChallengeList(List<DoneChallenge> doneChallengeList) {
        this.mDoneChallengeList = doneChallengeList;
    }



}

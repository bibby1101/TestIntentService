package com.bibby.testintentservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends Activity implements MyResultReceiver.Receiver, TvHIDService.CallBack,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView state;
//    public MyResultReceiver myResultReceiver;

    Intent serviceIntent;
    TvHIDService myService;

    final static int LOCATION_REQUEST_CODE = 9999;
    final static int GPS_REQUEST_CODE = 9527;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    LocationSettingsRequest.Builder mLSRBuild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(MainActivity.this, TvHIDService.class);

        state = (TextView) this.findViewById(R.id.state);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { // Android6.0
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                startService(serviceIntent); //Starting the service
                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

            } else {
                // TODO: 2017/6/14 此API目前還搞不懂邏輯
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, ACCESS_FINE_LOCATION)) {
                    Log.e(TAG, "需要顯示視窗給使用者");
                    ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
                else {
                    Log.e(TAG, "使用者選擇不再顯示");
                    ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
            }
        }


        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mLocationRequest = LocationRequest.create()
                .setInterval(10 * 60 * 1000) // every 10 minutes
                .setExpirationDuration(10 * 1000) // After 10 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLSRBuild = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        mLSRBuild.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLSRBuild.build());
        result.setResultCallback(callbackLSR);



        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click to bind service, thread id : " + Thread.currentThread().getId());

//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, TvHIDIntentService.class);
//                intent.putExtra("receiver", myResultReceiver);
//                startService(intent);


//                startService(serviceIntent); //Starting the service
//                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
//                Toast.makeText(MainActivity.this, "Button checked", Toast.LENGTH_SHORT).show();

            }
        });

//        myResultReceiver = new MyResultReceiver(new Handler());
//        myResultReceiver.setReceiver(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        startService(serviceIntent); //Starting the service
//        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        myResultReceiver.removeReceiver();
        if(myService!=null)
            unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult, thread id : " + Thread.currentThread().getId());
        Log.d(TAG, "onReceiveResult : " + resultCode + " | " + resultData.getString("Service"));
    }






    @Override
    public void updateClient(long data) {
        Log.d(TAG, "updateClient, thread id : " + Thread.currentThread().getId());
        Log.d(TAG, "updateClient : " + data);
    }

    @Override
    public void onConnectState(int oldstate, int nowstate) {

    }

    @Override
    public void onBondState(int oldstate, int nowstate) {

    }






    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            TvHIDService.LocalBinder binder = (TvHIDService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
//            myService.startCounter();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
//            myService.stopCounter();
        }
    };




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApiClient.ConnectionCallbacks onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient.ConnectionCallbacks onConnectionSuspended : " + i);
    }

    /*
    * https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult
    * **/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleApiClient.OnConnectionFailedListener onConnectionFailed : " + connectionResult.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startService(serviceIntent); //Starting the service
                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

            }
            else {
                // Permission was denied. Display an error message.
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GPS_REQUEST_CODE){
            switch (resultCode){
                case Activity.RESULT_OK:
                    Toast.makeText(MainActivity.this, "定位服務已開啟.", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(MainActivity.this, "必須開啟定位服務.", Toast.LENGTH_SHORT).show();
                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLSRBuild.build());
                    result.setResultCallback(callbackLSR);
                    break;
                default:
                    Log.d(TAG, "onActivityResult : unknow state : " + resultCode);
                    break;
            }
        }
    }

    ResultCallback<LocationSettingsResult> callbackLSR = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            final LocationSettingsStates state = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.e(TAG, "LocationSettingsStatusCodes.SUCCESS");
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.e(TAG, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                    try {
                        status.startResolutionForResult(MainActivity.this, GPS_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                    break;
            }
        }
    };
}

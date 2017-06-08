package com.bibby.testintentservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MyResultReceiver.Receiver, TvHIDService.CallBack {

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView state;
//    public MyResultReceiver myResultReceiver;

    Intent serviceIntent;
    TvHIDService myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = (TextView) this.findViewById(R.id.state);

        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click to bind service, thread id : " + Thread.currentThread().getId());


//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, TvHIDIntentService.class);
//                intent.putExtra("receiver", myResultReceiver);
//                startService(intent);

                startService(serviceIntent); //Starting the service
                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
                Toast.makeText(MainActivity.this, "Button checked", Toast.LENGTH_SHORT).show();
            }
        });

//        myResultReceiver = new MyResultReceiver(new Handler());
//        myResultReceiver.setReceiver(this);

        serviceIntent = new Intent(MainActivity.this, TvHIDService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

}

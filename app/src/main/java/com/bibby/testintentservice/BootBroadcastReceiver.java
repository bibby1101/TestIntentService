package com.bibby.testintentservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver implements TvHIDService.CallBack {

    public static final String TAG = BootBroadcastReceiver.class.getSimpleName();
    Context context;
    TvHIDService myService;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Log.d(TAG, "intent.getAction() : " + intent.getAction());

        this.context = context;

        if((intent.getAction()).equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent serviceIntent = new Intent(context, TvHIDService.class);
            context.startService(serviceIntent); //Starting the service
            context.bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
            Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Toast.makeText(context, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            TvHIDService.LocalBinder binder = (TvHIDService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(BootBroadcastReceiver.this); //Activity register in the service as client for callabcks!
//            myService.startCounter();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(context, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
//            myService.stopCounter();
        }
    };

    @Override
    public void updateClient(long data) {
        Log.d(TAG, "updateClient, thread id : " + Thread.currentThread().getId());
        Log.d(TAG, "updateClient : " + data);
    }
}

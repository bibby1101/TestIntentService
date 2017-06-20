package com.bibby.testintentservice;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BootBroadcastReceiver.class.getSimpleName();
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Log.d(TAG, "intent.getAction() : " + intent.getAction());

        this.context = context;

        if( intent==null && intent.getAction()==null ){
            Log.e(TAG, "intent is null or intent action is null");
            return;
        }

        if( intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ){
            Intent serviceIntent = new Intent(context, TvHIDService.class);
            context.startService(serviceIntent); //Starting the service
            Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
        }
        else if( intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED) ){
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.e(TAG, "STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.e(TAG, "STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.e(TAG, "STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.e(TAG, "STATE_TURNING_ON");
                    break;
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.e(TAG, "STATE_CONNECTED");
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    Log.e(TAG, "STATE_CONNECTING");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.e(TAG, "STATE_DISCONNECTED");
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    Log.e(TAG, "STATE_DISCONNECTING");
                    break;
                case BluetoothAdapter.ERROR:
                    Log.e(TAG, "ERROR");
                    break;
            }
        }
    }
}

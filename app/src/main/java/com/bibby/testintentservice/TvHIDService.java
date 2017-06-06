package com.bibby.testintentservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class TvHIDService extends Service {

    private static final String TAG = TvHIDService.class.getSimpleName();
    CallBack callBack;

    private long startTime = 0;
    private long millis = 0;

    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            callBack.updateClient(millis); //Update Activity (client) by the implementd callback
            handler.postDelayed(this, 1000);
        }
    };

    public interface CallBack {
        void updateClient(long data);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate, thread id : " + Thread.currentThread().getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, thread id : " + Thread.currentThread().getId());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand, thread id : " + Thread.currentThread().getId());

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind, thread id : " + Thread.currentThread().getId());

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind, thread id : " + Thread.currentThread().getId());

        return super.onUnbind(intent);
    }

    public void startCounter(){
        startTime = System.currentTimeMillis();
        handler.postDelayed(serviceRunnable, 0);
        Toast.makeText(getApplicationContext(), "Counter started", Toast.LENGTH_SHORT).show();
    }

    public void stopCounter(){
        handler.removeCallbacks(serviceRunnable);
    }

    public class LocalBinder extends Binder {
        public TvHIDService getServiceInstance(){
            return TvHIDService.this;
        }
    }

    public void registerClient(CallBack callBack){
        this.callBack = callBack;
    }
}

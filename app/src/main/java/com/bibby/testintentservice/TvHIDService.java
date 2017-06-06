package com.bibby.testintentservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class TvHIDService extends IntentService {

    private final static String TAG = TvHIDService.class.getSimpleName();
    private int i = 0;

    /**
     * Creates an IntentService.
     *
     * Constructor with no argument.
     */
    public TvHIDService(){
        super("TvHIDService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public TvHIDService(String name) {
        super(name);
        Log.d(TAG, "onConstructed, thread id : " + Thread.currentThread().getId());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent, thread id : " + Thread.currentThread().getId());

        ResultReceiver rec = intent.getParcelableExtra("receiver");

        Log.d(TAG, "onHandleIntent(), start work.");
        Log.d(TAG, "print start, init i = " + i);
        for(; i< 5; i++) {
            Log.d(TAG, "i = " + i);

            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "onHandleIntent(), end work.");

        Bundle b = new Bundle();
        b.putString("Service","i = "+i);
        rec.send(9527, b);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand, thread id : " + Thread.currentThread().getId());

//        return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind, thread id : " + Thread.currentThread().getId());
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind, thread id : " + Thread.currentThread().getId());
        return super.onUnbind(intent);
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
}

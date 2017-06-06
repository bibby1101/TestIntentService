package com.bibby.testintentservice;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements MyResultReceiver.Receiver{

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView state;
    public MyResultReceiver myResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        state = (TextView) this.findViewById(R.id.state);

        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click to bind service, thread id : " + Thread.currentThread().getId());
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TvHIDService.class);
                intent.putExtra("receiver", myResultReceiver);
                startService(intent);
            }
        });

        myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver(this);

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResult, thread id : " + Thread.currentThread().getId());
        Log.d(TAG, "onReceiveResult : " + resultCode + " | " + resultData.getString("Service"));
    }
}

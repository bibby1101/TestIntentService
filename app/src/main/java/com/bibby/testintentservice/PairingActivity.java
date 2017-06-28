package com.bibby.testintentservice;

import android.animation.ArgbEvaluator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PairingActivity extends AppCompatActivity implements TvHIDService.CallBack {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private PairingPagerAdapter pairingPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;


    private int bgColors[];
    private AppCompatButton buttonFinish;
    private ImageButton buttonPre;
    private ImageButton buttonNext;
    private ImageView[] indicators;
    private int currentPosition;
    private static final int MSG_DATA_INSERT_FINISH = 1;
    private Handler handler = new Handler(new HandlerCallback());
    private static final String TAG = PairingActivity.class.getSimpleName();

    Intent serviceIntent;
    TvHIDService myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        initViews();

        initData();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (Integer) new ArgbEvaluator().evaluate(positionOffset, bgColors[position], bgColors[position == 2 ? position : position + 1]);
                viewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateIndicators(position);
                viewPager.setBackgroundColor(bgColors[position]);
                buttonPre.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                buttonNext.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                buttonFinish.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FINISH");
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition += 1;
                viewPager.setCurrentItem(currentPosition, true);
            }
        });

        buttonPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition -= 1;
                viewPager.setCurrentItem(currentPosition, true);
            }
        });

        serviceIntent = new Intent(PairingActivity.this, TvHIDService.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pairing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.onboarding_indicator_selected : R.drawable.onboarding_indicator_unselected
            );
        }
    }



    @Override
    public void updateClient(long data) {

    }

    @Override
    public void onConnectState(int oldstate, int nowstate) {
        Log.d(TAG, "onConnectState : " + oldstate + " -> " + nowstate);
        if(nowstate==2){
            currentPosition += 1;
            viewPager.setCurrentItem(currentPosition, true);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 5000L);
        }
    }

    @Override
    public void onBondState(int oldstate, int nowstate) {
        Log.d(TAG, "onBondState : " + oldstate + " -> " + nowstate);
    }



    private class HandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_DATA_INSERT_FINISH:

                    buttonFinish.setText(R.string.onboarding_finish_button_description);
                    buttonFinish.setEnabled(true);
                    break;
            }
            return true;
        }
    }

    private void initViews() {
        pairingPagerAdapter = new PairingPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) this.findViewById(R.id.container);
        viewPager.setAdapter(pairingPagerAdapter);

        buttonFinish = (AppCompatButton) this.findViewById(R.id.buttonFinish);
        buttonFinish.setText(R.string.onboarding_finish_button_description_wait);
        buttonFinish.setEnabled(false);

        buttonNext = (ImageButton) this.findViewById(R.id.imageButtonNext);

        buttonPre = (ImageButton) this.findViewById(R.id.imageButtonPre);

        indicators = new ImageView[] {
                (ImageView) this.findViewById(R.id.imageViewIndicator0),
                (ImageView) this.findViewById(R.id.imageViewIndicator1),
                (ImageView) this.findViewById(R.id.imageViewIndicator2) };
    }

    private void initData() {
        bgColors = new int[]{ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.cyan_500),
                ContextCompat.getColor(this, R.color.light_blue_500)};
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myService!=null)
            unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Toast.makeText(PairingActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            TvHIDService.LocalBinder binder = (TvHIDService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(PairingActivity.this); //Activity register in the service as client for callabcks!
//            myService.startCounter();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(PairingActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
//            myService.stopCounter();
        }
    };
}

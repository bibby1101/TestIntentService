package com.bibby.testintentservice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
        void onConnectState(int oldstate, int nowstate);
        void onBondState(int oldstate, int nowstate);
    }



    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private IntentFilter mIntentFilter;
    private ArrayList<BluetoothDevice> mLeDevices;
    private BluetoothInputDevice mService;
    private boolean mIsProfileReady;

    private static final int PROXMITY_RSSI_THRESHOLD = -80;
    private static final int PROXMITY_PATHLOSS_THRESHOLD = 90;

    private static final int INVALID_TX_POWER = 0xffff;
    static final int TX_POWER_FLAG = 0x0a;
    static final int COMPLETE_NAME_FLAG = 0x09;
    static final int UUID16_SERVICE_FLAG_MORE = 0x02;
    static final int UUID16_SERVICE_FLAG_COMPLETE = 0x03;
    static final int UUID32_SERVICE_FLAG_MORE = 0x04;
    static final int UUID32_SERVICE_FLAG_COMPLETE = 0x05;
    static final int UUID128_SERVICE_FLAG_MORE = 0x06;
    static final int UUID128_SERVICE_FLAG_COMPLETE = 0x07;

    static final int HOGP_UUID16 = 0x1812;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5*1000;




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate, thread id : " + Thread.currentThread().getId());

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "裝置不支援BLE設備", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_UUID);
        mIntentFilter.addAction(BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, mIntentFilter);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBluetoothAdapter.getProfileProxy(this, new InputDeviceServiceListener(),
                BluetoothProfile.INPUT_DEVICE);

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "裝置不支援藍芽", Toast.LENGTH_SHORT).show();
            stopSelf();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy, thread id : " + Thread.currentThread().getId());

        unregisterReceiver(mBluetoothReceiver);
        scanLeDevice(false);

        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.INPUT_DEVICE, mService);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand, thread id : " + Thread.currentThread().getId());

        scanLeDevice(true);

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind, thread id : " + Thread.currentThread().getId());

        scanLeDevice(true);
//        registerReceiver(mBluetoothReceiver, mIntentFilter);

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




    void doHogpConnect(BluetoothDevice device){
        Log.d(TAG, "doHogpConnect");

        ParcelUuid[] uuids = device.getUuids();
        if (uuids == null){
            Log.d(TAG, "uuid is null");
//            return;
        }

        Log.v(TAG, "uuid update change event is received");

        if((device.getType()== BluetoothDevice.DEVICE_TYPE_CLASSIC)) {
            Log.v(TAG, "Not a LE device, ignore");
//            return;
        }

        if(!BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hogp)) {
            Log.v(TAG, "Not support HOGP, ignore");
//            return;
        }

        if(mService !=null){
//            mService.disconnect(device);
            mService.connect(device);
        } else {
            Log.v(TAG, "Bluetooth HID serivce is not ready");
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private final class InputDeviceServiceListener
            implements BluetoothProfile.ServiceListener {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG,"Bluetooth service connected");
            mService = (BluetoothInputDevice) proxy;
            // We just bound to the service
            mIsProfileReady=true;
            List<BluetoothDevice> deviceList = mService.getConnectedDevices();

            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                if((nextDevice.getType() & BluetoothDevice.DEVICE_TYPE_LE) == BluetoothDevice.DEVICE_TYPE_LE) {
                    // TODO: 2017/6/8 ?
                }
            }
        }

        public void onServiceDisconnected(int profile) {
            Log.d(TAG,"Bluetooth service disconnected");
            mIsProfileReady=false;
            mService = null;
        }
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "Received intent: " + action) ;
            BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//          if((!mLeDevices) ||
//              (mLeDevices && !mLeDevices.contains(remoteDevice))) {
//              Log.v(TAG, "Intents for devices that we do not care, ignore");
//          }

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                Log.v(TAG, "Bond state change event is received");

                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                                   BluetoothDevice.ERROR);
                int preBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                                                   BluetoothDevice.ERROR);

                switch (preBondState) {
                    case 10:
                        Log.d(TAG, "prebondState : BOND_NONE(10)");
                        break;
                    case 11:
                        Log.d(TAG, "prebondState : BOND_BONDING(11)");
                        break;
                    case 12:
                        Log.d(TAG, "prebondState : BOND_BONDED(12)");
                        break;
                    default:
                        Log.d(TAG, "prebondState : BOND_ERROR("+preBondState+")");
                }
                Log.d(TAG, "bondState : " + (
                    bondState==10?"BOND_NONE(10)":
                    bondState==11?"BOND_BONDING(11)":
                    bondState==12?"BOND_BONDED(12)":"BOND_ERROR("+bondState+")")
                );

                if(preBondState==11&&bondState==10){
                    Intent i = new Intent();
                    i.setClass(TvHIDService.this, PairingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }

                if(callBack!=null){
                    callBack.onBondState(preBondState, bondState);
                }

                // BluetoothDevice.ERROR -2147483648
                // BluetoothDevice.BOND_NONE 10
                // BluetoothDevice.BOND_BONDING 11
                // BluetoothDevice.BOND_BONDED 12

                // TODO: 2017/6/8 無法配對時 10 -> 11 and 11 -> 10
                // TODO: 2017/6/19 可配對時 10 -> 11 and 11 -> 12

            } else if(action.equals(BluetoothDevice.ACTION_UUID)){
                doHogpConnect(remoteDevice);
            } else if(action.equals(BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED)) {
                Log.v(TAG, "Connection state changed");

                int profileState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                int preProfileState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE,
                        BluetoothAdapter.ERROR);

                switch (preProfileState) {
                    case 0:
                        Log.d(TAG, "preProfileState : STATE_DISCONNECTED(0)");
                        break;
                    case 1:
                        Log.d(TAG, "preProfileState : STATE_CONNECTING(1)");
                        break;
                    case 2:
                        Log.d(TAG, "preProfileState : STATE_CONNECTED(2)");
                        break;
                    case 3:
                        Log.d(TAG, "preProfileState : STATE_DISCONNECTING(3)");
                        break;
                    default:
                        Log.d(TAG, "preProfileState : STATE_ERROR("+preProfileState+")");
                }
                Log.d(TAG, "profileState : " + (
                    profileState==0?"STATE_DISCONNECTED(0)":
                    profileState==1?"STATE_CONNECTING(1)":
                    profileState==2?"STATE_CONNECTED(2)":
                    profileState==3?"STATE_DISCONNECTING(3)":"STATE_ERROR("+profileState+")")
                );

                if(preProfileState==1&&profileState==0){
                    Intent i = new Intent();
                    i.setClass(TvHIDService.this, PairingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }

                if(callBack!=null){
                    callBack.onConnectState(preProfileState, profileState);
                }

                // BluetoothAdapter.ERROR -2147483648
                // BluetoothProfile.STATE_CONNECTING 1
                // BluetoothProfile.STATE_CONNECTED 2
                // BluetoothProfile.STATE_DISCONNECTING 3
                // BluetoothProfile.STATE_DISCONNECTED 0

                // TODO: 2017/6/19 0 -> 1 and 1 -> 0 無法與遙控器通訊
                // TODO: 2017/6/19 0 -> 1 and 1 -> 2 連線成功
                // TODO: 2017/6/19 1 -> 2 and 2 -> 0 太久沒用自動斷線 or 螢幕關閉

            }
        }
    };

    private int extractTxPower(byte[] scanRecord){
        int i, length=scanRecord.length;
        i = 0;

        while (i< length-2) {
            int element_len = scanRecord[i];
            byte element_type = scanRecord[i+1];
            if(element_type == 0x0a) {
                Log.i(TAG, "extractTxPower Bingo, we TX power=" + scanRecord[i+2]);
                return scanRecord[i+2];
            }
            i+= element_len+1;
        }

        return INVALID_TX_POWER;
    }

    /*lgh we only care 16bit UUID now*/
    private boolean containHogpUUID(byte[] scanRecord){
        int i, j, length=scanRecord.length;
        i = 0;
        int uuid = 0;
        while (i< length-2) {
            int element_len = scanRecord[i];
            byte element_type = scanRecord[i+1];
            if(element_type == UUID16_SERVICE_FLAG_MORE
                    ||element_type == UUID16_SERVICE_FLAG_COMPLETE ) {
                for(j=0; j<element_len-1;j++,j++)
                {
                    uuid = scanRecord[i+j+2]+(scanRecord[i+j+3]<<8);
//                    Log.i(TAG, "containHogpUUID Got UUID uuid=0x" + Integer.toHexString(uuid));
                    if (uuid == HOGP_UUID16) {
                        return true;
                    }
                }
            } else if (element_type >= UUID32_SERVICE_FLAG_MORE
                    && element_type >= UUID128_SERVICE_FLAG_COMPLETE){
//                Log.i(TAG, "Do not support parsing 32bit or 12bit UUID now");
            }
            i+= element_len+1;
        }

        return false;
    }

    void printArray(byte[] array, String preTag) {
        if (array != null) {
            for (int i=0; i < array.length; i++) {
                Log.v(TAG, preTag + " result:0X" + Integer.toHexString((int)(array[i] & 0x000000FF)));
            }
        } else {
            Log.e(TAG, preTag + " return array is null!");
        }
    }

    private boolean isNameMatchextracName(byte[] scanRecord){
        int i, length=scanRecord.length;
        i = 0;
        byte[] RcName= new byte[50];
        String decodedName = null;
        while (i< length-2) {
            int element_len = scanRecord[i];
            byte element_type = scanRecord[i+1];
            if(element_type == COMPLETE_NAME_FLAG) {
                System.arraycopy(scanRecord, i+2, RcName, 0, element_len-1);
                try {
                    decodedName = new String(RcName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }

                String XiaomiRcName = new String("Opal RC"); // Opal RC // Xiaomi Remote
                if(decodedName.startsWith(XiaomiRcName) == true) {
//                    Log.i(TAG, "we found our RC");
                    return true;
                }
            }
            i+= element_len+1;
        }

        return false;
    }

    private boolean isGoodHogpRc(final int rssi, byte[] scanRecord) {
        int tx_power = extractTxPower(scanRecord);
        boolean isHogpDevice = containHogpUUID(scanRecord);
        boolean isXiaomiRc = isNameMatchextracName(scanRecord);
        if(isXiaomiRc == true && isHogpDevice == true) {
//            if((tx_power-rssi) <= PROXMITY_PATHLOSS_THRESHOLD) {
//            if(rssi >= PROXMITY_RSSI_THRESHOLD) {
//                Log.i(TAG, "we found our RC that is closed enough");
//                return true;
//            }
        }
        return false;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    if(isGoodHogpRc(rssi, scanRecord) ==  true) {

//                        Log.i(TAG, "onLeScan device="+ device + "rssi=" + rssi);

//                        doHogpConnect(device);

                        if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                            if(mScanning) {
                                mScanning = false;
                                if(device.createBond() == false) {
                                    Log.i(TAG, "Start bond failed="+ device);
                                }
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mScanning = true;
                                    }
                                }, SCAN_PERIOD);
                            }
                        } else if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                            if(mScanning) {

                                Log.i(TAG, "Connect directly=" + device);
                                Log.i(TAG, "we found our RC");

                                if(rssi >= PROXMITY_RSSI_THRESHOLD) {
                                    Log.i(TAG, "we found our RC that is closed enough");
                                }

                                mScanning = false;
                                doHogpConnect(device);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mScanning = true;
                                    }
                                }, SCAN_PERIOD);
                            }
                        }

                    } else {

                    }
                }
            };
}

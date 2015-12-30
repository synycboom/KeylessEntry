package cn457.keylessentry;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class EntryService extends Service {

    private ServiceThread mSendingServiceWorker;
    private int stopTimeSec = 120;
    private boolean isUnlocked = false;
    private int keyCount = 0;
    public static Object connectMasterKeyLock = new Object();
    List<String> keys = new ArrayList<String>();
    List<Device> mDevices = new ArrayList<Device>();

    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        stopSelf();
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mScanningDeviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("Discover", "Start discovering");

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("Discover", "Finish discovering");

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevices.add(new Device(device.getAddress(), device.getName(), device));
            }
        }
    };


    private final class ServiceThread extends Thread {

        private final BroadcastReceiver mUnlockResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(action.equals(BluetoothControl.BLUETOOTH_UNLOCK_ACTION)){
                    Bundle extras = intent.getExtras();
                    int result = extras.getInt(BluetoothControl.UNLOCK_RESULT);
                    switch (result){
                        case BluetoothControl.UNLOCK_REQUESTPASS_SUCCESS:
                            if( keyCount < keys.size()) {
                                BluetoothControl.getInstance().getConnection().write(keys.get(keyCount));
                            }
                            break;
                        //this is not masterkey
                        case BluetoothControl.UNLOCK_REQUESTPASS_FAILED:
                            Log.i("STATE", "THIS IS NOT MASTERKEY");
                            synchronized (connectMasterKeyLock) {
                                try {
                                    connectMasterKeyLock.notify();
                                } catch (Exception e) {
                                    Log.i("Exception", e.toString());
                                }
                            }
                            break;

                        case BluetoothControl.UNLOCK_SUCCESS:
                            Log.i("STATE", "UNLOCK SUCCESS");
                            isUnlocked = true;
                            synchronized (connectMasterKeyLock) {
                                try {
                                    connectMasterKeyLock.notify();
                                } catch (Exception e) {
                                    Log.i("Exception", e.toString());
                                }
                            }
                            break;
                        case BluetoothControl.UNLOCK_FAILED:
                            //notify sendingKeyLock untill the keys is all used
                            if(keyCount < keys.size()) {
                                BluetoothControl.getInstance().getConnection().write(keys.get(keyCount));
                                Log.i("SENDING PASSWORD", keys.get(keyCount));
                                keyCount++;
                            }

                            //all key cannot unlock
                            if(keyCount == keys.size()){
                                Log.i("STATE", "UNLOCK FAILED");
                                BluetoothControl.getInstance().getConnection().write("UnlockFailed");
                            }
                            break;
                        case BluetoothControl.UNLOCK_STOP:
                            synchronized (connectMasterKeyLock) {
                                try {
                                    connectMasterKeyLock.notify();
                                } catch (Exception e) {
                                    Log.i("Exception", e.toString());
                                }
                            }
                            break;
                    }
                }
            }
        };

        @Override
        public void run() {
            registerReceiver(mUnlockResultReceiver, new IntentFilter(BluetoothControl.BLUETOOTH_UNLOCK_ACTION));

            if(BluetoothControl.getInstance().getAdapter() != null)
                BluetoothControl.getInstance().getAdapter().startDiscovery();

            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + stopTimeSec * 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (connectMasterKeyLock) {
                    try {
                        if(BluetoothControl.getInstance().getAdapter().isDiscovering() && mDevices.isEmpty())
                            continue;

                        for(Device dev: mDevices){
                            BluetoothControl.getInstance().setConnection(new ConnectThread(dev.getDeviceObj(), getApplicationContext(), true));
                            BluetoothControl.getInstance().getConnection().start();
                            Log.i("UnlockRequest", dev.getDeviceName());
                            //wait for connection finish (will be notified)
                            connectMasterKeyLock.wait();
                            if(isUnlocked){
                                Log.i("UNLOCK STATUS", dev.getDeviceName() + " is unlocked");
                                isUnlocked = false;
                            }
                            BluetoothControl.getInstance().resetConnection();
                        }
                        mDevices.clear();
                        BluetoothControl.getInstance().getAdapter().startDiscovery();

                    } catch (Exception e) {
                        Log.i("Exception", e.toString());
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            unregisterReceiver(mUnlockResultReceiver);
            stopSelf();
        }
    }

    /********************************************************************************************
     *
     ********************************************************************************************/

    @Override
    public void onCreate() {
        keys.add("asdasd");
        keys.add("asdaa");
        keys.add("nklk");
        keys.add("ertert");
        keys.add("asdj");
        keys.add("xxx");

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        mSendingServiceWorker = new ServiceThread();
        mSendingServiceWorker.start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mScanningDeviceReceiver, filter);
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service is running", Toast.LENGTH_SHORT).show();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        unregisterReceiver(mScanningDeviceReceiver);
        unregisterReceiver(mBluetoothStateReceiver);
    }
}

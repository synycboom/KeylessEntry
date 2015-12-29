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
    private Looper mServiceSending;
    private Looper mServiceResult;
    private SendServiceHandler mSendingServiceHandler;
    private SendServiceHandler mResultServiceHandler;
    private int stopTimeSec = 120;
    private boolean isUnlocked = false;
    private int keyCount = 0;
    public static Object connectMasterKeyLock = new Object();
    public static Object sendingKeyLock = new Object();
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
//                Log.i("Device","Found device " + device.getName());
            }
        }
    };


    // Handler that receives messages from the thread
    private final class SendServiceHandler extends Handler {

        private final BroadcastReceiver mUnlockSendingKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(action.equals(BluetoothControl.BLUETOOTH_SEND_ACTION)){
                    Bundle extras = intent.getExtras();
                    int result = extras.getInt(BluetoothControl.UNLOCK_SEND_RESULT);
                    switch (result){
                        //send password to masterkey
                        case BluetoothControl.UNLOCK_REQUESTPASS_SUCCESS:
                            if( keyCount < keys.size()){
                                synchronized (sendingKeyLock) {
                                    try {
                                        Log.i("KEY COUNT SEND", " "+keyCount);
                                        BluetoothControl.getInstance().getConnection().write(keys.get(keyCount));
                                        Log.i("SENDING PASSWORD", keys.get(keyCount));
                                        wait();
                                    } catch (Exception e) {}
                                }
                                if(isUnlocked){
                                    keyCount = 0;
                                    break;
                                }
                            }
                            else{
                                keyCount = 1;
                                synchronized (connectMasterKeyLock) {
                                    try {
                                        notify();
                                    } catch (Exception e) {}
                                }
                            }
                            break;
                        //this is not masterkey
                        case BluetoothControl.UNLOCK_REQUESTPASS_FAILED:
                            Log.i("STATE", "THIS IS NOT MASTERKEY");
                            synchronized (connectMasterKeyLock) {
                                try {
                                    notify();
                                } catch (Exception e) {}
                            }
                            break;
                    }
                }
            }
        };

        public SendServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            registerReceiver(mUnlockSendingKeyReceiver, new IntentFilter(BluetoothControl.BLUETOOTH_SEND_ACTION));

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
                            BluetoothControl.getInstance().getConnection().cancel();
                        }
//                        wait(endTime - System.currentTimeMillis());
                        mDevices.clear();
                        BluetoothControl.getInstance().getAdapter().startDiscovery();

                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    /********************************************************************************************
     *
     ********************************************************************************************/

    // Handler that receives messages from the thread
    private final class ResultServiceHandler extends Handler {

        private final BroadcastReceiver mUnlockResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(action.equals(BluetoothControl.BLUETOOTH_UNLOCK_ACTION)){
                    Bundle extras = intent.getExtras();
                    int result = extras.getInt(BluetoothControl.UNLOCK_RESULT);
                    Log.i("STATE", "UNLOCK RESULTTTTTTTTTTTT :::" + result);
                    switch (result){
                        case BluetoothControl.UNLOCK_SUCCESS:
                            Log.i("STATE", "UNLOCK SUCCESS");
                            isUnlocked = true;
                            //if unlocked have to notify sendingKey to renew key
                            synchronized (sendingKeyLock) {
                                try {
                                    notify();
                                } catch (Exception e) {}
                            }

                            synchronized (connectMasterKeyLock) {
                                try {
                                    notify();
                                } catch (Exception e) {}
                            }
                            break;
                        case BluetoothControl.UNLOCK_FAILED:
                            Log.i("STATE", "UNLOCK FAILED");

                            //notify sendingKeyLock untill the keys is all used
                            if(keyCount < keys.size()) {
                                keyCount++;
                                Log.i("KEY COUNT UNLOCK", " "+keyCount);
                                synchronized (sendingKeyLock) {
                                    try {
                                        notify();
                                    } catch (Exception e) {}
                                }
                            }
                            break;
                    }
                }
            }
        };

        public ResultServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            registerReceiver(mUnlockResultReceiver, new IntentFilter(BluetoothControl.BLUETOOTH_UNLOCK_ACTION));

            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + stopTimeSec * 1000;
            while (System.currentTimeMillis() < endTime) {

            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        keys.add("asdasd");
        keys.add("asdasdzx");
        keys.add("zxce");
        keys.add("ertert");
        keys.add("asdj");

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);

        HandlerThread thread2 = new HandlerThread("ServiceStartArguments2",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);

        thread.start();
        thread2.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceSending = thread.getLooper();
        mServiceResult  = thread2.getLooper();
        mSendingServiceHandler = new SendServiceHandler(mServiceSending);
        mResultServiceHandler  = new SendServiceHandler(mServiceResult);



        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mScanningDeviceReceiver, filter);
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        BluetoothControl.getInstance().getAdapter().startDiscovery();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting after 15 sec", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mSendingServiceHandler.obtainMessage();
        Message msg2 = mResultServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg2.arg1 = startId+1;
        mSendingServiceHandler.sendMessage(msg);
        mResultServiceHandler.sendMessage(msg2);

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
//        unregisterReceiver(mUnlockResultReceiver);
//        unregisterReceiver(mUnlockSendingKeyReceiver);
    }
}

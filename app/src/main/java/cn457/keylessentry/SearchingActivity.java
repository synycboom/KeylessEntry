package cn457.keylessentry;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SearchingActivity extends Activity {

    private ListView mListView;
    private CustomAdapter mAdapter;
    private Button cancelButton;
    private Button okButton;
    private Button scanButton;
    private LinearLayout buttonSection;
    private LinearLayout listViewSection;

    private int connectionType;
    private BluetoothAdapter mBluetoothAdapter = null;
    private final int REQUEST_DISCOVERABLE_TIME = 10;


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
                        backToMainActivity();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i("Bluetooth", "Turning Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i("Bluetooth", "On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i("Bluetooth", "Turning on");
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
                //set result of devices to adapter listview

                if(!mDevices.isEmpty()){
                    buttonSection.removeAllViews();
                    generateCancelButton();
                    generateOkButton();
                    showListViewOfDevices();
                }
                else{ //Scan devices again if there is no device found

                }

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevices.add(new Device(device.getAddress(),device.getName()));
                Log.i("Device","Found device " + device.getName());
            }
        }
    };

    private final BroadcastReceiver mDiscoverableStateReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {

                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.i("Discover","Discoverable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.i("Discover","Connectable");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.i("Discover","None");
                        break;
                }


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                connectionType = 0;
            } else {
                connectionType= extras.getInt(BluetoothControl.CONNECTION_TAG);
            }
        } else {
            connectionType= (int) savedInstanceState.getSerializable(BluetoothControl.CONNECTION_TAG);
        }
        setContentView(R.layout.activity_searching);
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        buttonSection = (LinearLayout) findViewById(R.id.searching_button_section);
        listViewSection = (LinearLayout) findViewById(R.id.searching_listview_section);

        generateScanButton();
        generateCancelButton();

        if(connectionType == BluetoothControl.SEARCHING)
            startSearching();
        if(connectionType == BluetoothControl.WAITING)
            startWaiting();

    }
    @Override
    protected void onPause(){
        super.onPause();
        if (BluetoothControl.getInstance().getAdapter().isDiscovering()) {
            BluetoothControl.getInstance().getAdapter().cancelDiscovery();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!BluetoothControl.getInstance().getAdapter().isDiscovering()) {
            BluetoothControl.getInstance().getAdapter().startDiscovery();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateReceiver);
        if(connectionType == BluetoothControl.SEARCHING){
            unregisterReceiver(mScanningDeviceReceiver);
        }
        if(connectionType == BluetoothControl.WAITING){
            unregisterReceiver(mDiscoverableStateReceiver);
        }
        if (BluetoothControl.getInstance().getAdapter().isDiscovering()) {
            BluetoothControl.getInstance().getAdapter().cancelDiscovery();
        }

    }


    private void startSearching(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mScanningDeviceReceiver, filter);
        if( ! BluetoothControl.getInstance().getAdapter().isDiscovering())
            BluetoothControl.getInstance().getAdapter().startDiscovery();
    }

    private void startWaiting(){
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_DISCOVERABLE_TIME);
        startActivity(discoverableIntent);

        registerReceiver(mDiscoverableStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
    }

    private void showListViewOfDevices(){
        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new CustomAdapter(this, mDevices);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setChecked(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void generateOkButton(){
        okButton = new Button(this);
        okButton.setText("OK");
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainActivity();
            }
        });

        LinearLayout.LayoutParams lp = getButtonParam(1);
        buttonSection.addView(okButton, lp);
    }

    private void generateCancelButton(){
        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainActivity();
            }
        });
        LinearLayout.LayoutParams lp = getButtonParam(1);
        buttonSection.addView(cancelButton, lp);
    }

    private void generateScanButton(){
        scanButton = new Button(this);
        scanButton.setText("Scan");
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainActivity();
            }
        });
        LinearLayout.LayoutParams lp = getButtonParam(1);
        buttonSection.addView(scanButton, lp);
    }

    private LinearLayout.LayoutParams getButtonParam(int weight){
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,weight);
    }

    private void backToMainActivity(){
        startActivity(new Intent(SearchingActivity.this, MainActivity.class));
    }


}

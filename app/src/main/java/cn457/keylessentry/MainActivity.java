package cn457.keylessentry;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends android.app.Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turnOnBluetooth();

        Button remoteKeyButton = (Button) findViewById(R.id.manage_remote_key_button);
        Button localKeyButton = (Button) findViewById(R.id.manage_local_key_button);
        Button entryButton = (Button) findViewById(R.id.entering_button);
        Button shareKey = (Button) findViewById(R.id.sharing_button);

        remoteKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchingActivity.class);
                intent.putExtra(BluetoothControl.CONNECTION_TAG, BluetoothControl.SEARCHING);
                if (!BluetoothControl.getInstance().getAdapter().isEnabled()) {
                    Toast prevent = Toast.makeText(MainActivity.this, "Please turn on bluetooth", Toast.LENGTH_SHORT);
                    prevent.show();
                } else if (isMyServiceRunning(EntryService.class)) {
                    Toast prevent = Toast.makeText(MainActivity.this, "Service is running", Toast.LENGTH_SHORT);
                    prevent.show();
                } else {
                    startActivity(intent);
                }
            }
        });

        localKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageLocalKeyActivity.class);
                startActivity(intent);
            }
        });

        shareKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SharingActivity.class);
                startActivity(intent);
            }
        });

        entryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, UnlockModeActivity.class);
                startActivity(intent);
            }
        });
    }

    public void turnOnBluetooth(){
        //check if device support bluetooth
        BluetoothControl.getInstance().setAdapter(BluetoothAdapter.getDefaultAdapter());
        if (BluetoothControl.getInstance().getAdapter() == null) {
            Log.i("Bluetooh", "not support!");
        }

        //request to turn on bluetooth
        if (!BluetoothControl.getInstance().getAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothControl.getInstance().REQUEST_ENABLE_BT);
        }
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == BluetoothControl.getInstance().REQUEST_ENABLE_BT){
                Log.i("Bluetooth", "Bluetooth is turned on");
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

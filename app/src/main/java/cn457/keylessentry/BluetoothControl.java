package cn457.keylessentry;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class BluetoothControl {

    private static BluetoothControl mInstance = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ConnectThread connection = null;

    public final int REQUEST_ENABLE_BT = 1;
    public static String CONNECTION_TAG = "Connection Type";
    public static int SEARCHING = 1;
    public static int WAITING = 2;
    public static final String BLUETOOTH_SOCKET = "STATE_OF_BLUETOOTH";
    public static final String CONNECTION_RESULT = "result";
    public static final int CONNECTION_FAILED = 0;
    public static final int CONNECTION_SUCCESS = 1;

    private BluetoothControl(){

    }

    public static BluetoothControl getInstance(){
        if(mInstance == null)
        {
            mInstance = new BluetoothControl();
        }
        return mInstance;
    }

    public BluetoothAdapter getAdapter(){
        return mBluetoothAdapter;
    }

    public void setConnection(ConnectThread connection){
        this.connection = connection;
    }

    public ConnectThread getConnection(){
        return connection;
    }

    public void setAdapter(BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
    }
}

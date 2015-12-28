package cn457.keylessentry;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class BluetoothControl {

    private static BluetoothControl mInstance = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    public final int REQUEST_ENABLE_BT = 1;
    public static String CONNECTION_TAG = "Connection Type";
    public static int SEARCHING = 1;
    public static int WAITING = 2;

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

    public void setAdapter(BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
    }
}

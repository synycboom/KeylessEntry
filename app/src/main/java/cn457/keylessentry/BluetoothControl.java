package cn457.keylessentry;

import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;
import java.util.List;

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
    /**Connection Action**/
    public static final String BLUETOOTH_CONNECTION_ACTION = "STATE_OF_BLUETOOTH";
    public static final String CONNECTION_RESULT = "result";
    public static final int CONNECTION_FAILED = 0;
    public static final int CONNECTION_SUCCESS = 1;

    /**Unlock Action**/
    public static final String BLUETOOTH_UNLOCK_ACTION = "BLUETOOTH_UNLOCK_ACTION";
    public static final String UNLOCK_RESULT = "UNLOCK_RESULT";
    public static final int UNLOCK_FAILED = 2;
    public static final int UNLOCK_SUCCESS = 3;
    public static final int UNLOCK_REQUESTPASS_FAILED = 4;
    public static final int UNLOCK_REQUESTPASS_SUCCESS = 5;
    public static final int UNLOCK_STOP = 6;

    /**Authentication Action**/
    public static final String AUTHENTICATION_ACTION = "AUTHENTICATION_ACTION";
    public static final String AUTHENTICATION_RESULT = "AUTHENTICATION_RESULT";
    public static final int AUTHENTICATION_FAILED = 7;
    public static final int AUTHENTICATION_SUCCESS = 8;

    /**ManageKey Action**/
    public static final String MANAGEKEY_ACTION = "MANAGEKEY_ACTION";
    public static final String MANAGEKEY_RESULT = "MANAGEKEY_RESULT";
    public static final String MANAGEKEY_KEYS = "MANAGEKEY_KEYS";
    public static final int MANAGEKEY_ADD_FAILED = 9;
    public static final int MANAGEKEY_ADD_SUCCESS = 10;
    public static final int MANAGEKEY_SHOW_FAILED = 11;
    public static final int MANAGEKEY_SHOW_SUCCESS = 12;
    public static final int MANAGEKEY_REMOVE_FAILED = 13;
    public static final int MANAGEKEY_REMOVE_SUCCESS = 14;
    public static final int MANAGEKEY_SIGNOUT_SUCCESS = 15;


    public static final String KEY_TAG = "key_tag";
    public static List<Key> keys;

    private BluetoothControl(){
        keys = new ArrayList<>();
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

    public void resetConnection(){
        if (connection == null)
            return;

        connection.cancel();
        connection = null;
    }

    public void setAdapter(BluetoothAdapter mBluetoothAdapter){
        this.mBluetoothAdapter = mBluetoothAdapter;
    }
}

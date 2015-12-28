package cn457.keylessentry;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final UUID MY_UUID;
    private Context context;

    private ConnectedThread connection = null;

    public ConnectThread(BluetoothDevice device, Context context) {
        this.context = context;
        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
        Log.i("Callback", "Prepare");
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        BluetoothControl.getInstance().getAdapter().cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            connection = new ConnectedThread(mmSocket, context);
            connection.start();
            write("AuthenticationRequest");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
                callbackToActivity(BluetoothControl.CONNECTION_FAILED);
            } catch (IOException closeException) { }
            return;
        }
    }

    public void callbackToActivity(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.BLUETOOTH_SOCKET);
        intent.putExtra(BluetoothControl.CONNECTION_RESULT, result);
        context.sendBroadcast(intent);
    }

    public void write(String message){
        byte[] b = message.getBytes(Charset.forName("UTF-8"));
        connection.write(b);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

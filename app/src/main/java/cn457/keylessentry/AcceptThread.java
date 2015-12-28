package cn457.keylessentry;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final UUID MY_UUID;
    public AcceptThread() {
        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = BluetoothControl.getInstance().getAdapter().listenUsingRfcommWithServiceRecord("testAPP",MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
//                manageConnectedSocket(socket);

//                mmServerSocket.close();
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
package cn457.keylessentry;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Created by synycboom on 12/28/2015 AD.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Scanner input;
    private Context context;

    public ConnectedThread(BluetoothSocket socket, Context context) {
        this.context = context;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        input = new Scanner(mmInStream);
    }

    public void run() {
        String message = "";
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {

                message += input.nextLine();
                if(message.contains("END")){
                    message = message.replace("END","");
                    Log.i("Result text", message);
                    if(message.contains("AuthenticationStart")){
                        callbackToActivity(BluetoothControl.CONNECTION_SUCCESS);
                    }else{
                        callbackToActivity(BluetoothControl.CONNECTION_FAILED);
                    }
                    message = "";
                }

            } catch (Exception e) {
                callbackToActivity(BluetoothControl.CONNECTION_FAILED);
                break;
            }
        }
    }






    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    public void callbackToActivity(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.BLUETOOTH_SOCKET);
        intent.putExtra(BluetoothControl.CONNECTION_RESULT, result);
        context.sendBroadcast(intent);
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
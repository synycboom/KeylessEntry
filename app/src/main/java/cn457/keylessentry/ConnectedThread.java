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
    private final int START = 0;
    private final int AUTHEN = 1;
    private final int ADMIN = 2;
    private final int UNLOCK = 3;
    private int state = START;
    private boolean isServiceRunning = false;

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Scanner input;
    private Context context;

    public ConnectedThread(BluetoothSocket socket, Context context,boolean isServiceRunning) {
        this.isServiceRunning = isServiceRunning;
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

                    switch (state){
                        case START:
                            startState(message);
                            break;
                        case AUTHEN:
                            authenState(message);
                            break;
                        case ADMIN:
                            adminState(message);
                            break;
                        case UNLOCK:
                            unlockState(message);
                            break;
                    }
                    message = "";
                }

            } catch (Exception e) {
                callbackToSearchingActivity(BluetoothControl.CONNECTION_FAILED);
                callbackToService(BluetoothControl.UNLOCK_REQUESTPASS_FAILED);
                break;
            }
        }
    }






    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        }
        catch (IOException e) { }
    }

    public void callbackToSearchingActivity(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.BLUETOOTH_CONNECTION_ACTION);
        intent.putExtra(BluetoothControl.CONNECTION_RESULT, result);
        context.sendBroadcast(intent);
    }

    public void callbackToAuthenticationActivity(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.AUTHENTICATION_ACTION);
        intent.putExtra(BluetoothControl.AUTHENTICATION_RESULT, result);
        context.sendBroadcast(intent);
    }

    public void callbackToManageKeyActivity(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.MANAGEKEY_ACTION);
        intent.putExtra(BluetoothControl.MANAGEKEY_RESULT, result);
        context.sendBroadcast(intent);
    }

    public void callbackToManageKeyActivity(int result,String extra){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.MANAGEKEY_ACTION);
        intent.putExtra(BluetoothControl.MANAGEKEY_RESULT, result);
        intent.putExtra(BluetoothControl.MANAGEKEY_KEYS, extra);
        context.sendBroadcast(intent);
    }

    public void callbackToService(int result){
        Intent intent = new Intent();
        intent.setAction(BluetoothControl.BLUETOOTH_UNLOCK_ACTION);
        intent.putExtra(BluetoothControl.UNLOCK_RESULT, result);
        context.sendBroadcast(intent);
    }

    public void setContext(Context context){
        this.context = context;
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            if(mmOutStream != null)
                mmOutStream.close();
            if(input != null)
                input.close();
            if(mmInStream != null)
                mmInStream.close();
            if(mmSocket != null)
                mmSocket.close();
        } catch (IOException e) { }
    }



    private void startState(String message){
        if(isServiceRunning){
            if(message.contains("UnlockStart")){
                state = UNLOCK;
                callbackToService(BluetoothControl.UNLOCK_REQUESTPASS_SUCCESS);
            }else{
                Log.i("DEBUG", message);
                callbackToService(BluetoothControl.UNLOCK_REQUESTPASS_FAILED);
            }
            return;
        }

        if(message.contains("AuthenticationStart")){
            state = AUTHEN;
            callbackToSearchingActivity(BluetoothControl.CONNECTION_SUCCESS);
        }else{
            callbackToSearchingActivity(BluetoothControl.CONNECTION_FAILED);
        }
    }

    private void unlockState(String message){
        if(message.contains("Welcome")){
            callbackToService(BluetoothControl.UNLOCK_SUCCESS);
        }else if(message.contains("TryAgain")){
            callbackToService(BluetoothControl.UNLOCK_FAILED);
        }
        else{
            callbackToService(BluetoothControl.UNLOCK_STOP);
        }
    }

    private void authenState(String message){
        if(message.contains("Valid")){
            state = ADMIN;
            callbackToAuthenticationActivity(BluetoothControl.AUTHENTICATION_SUCCESS);
        }else{
            callbackToAuthenticationActivity(BluetoothControl.AUTHENTICATION_FAILED);
        }
    }

    private void adminState(String message){
        if(message.contains("KeysAre:")){
            message = message.replace("KeysAre:","");
            Log.i("DEBUG", message);
            if(message.equals("")){
                BluetoothControl.keys.clear();
                callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_SHOW_SUCCESS,"");
                return;
            }

            callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_SHOW_SUCCESS, message);
        }
        if(message.contains("RemoveSuccess")){
            message = message.replace("RemoveSuccess->","");
            Log.i("DEBUG", message);
            callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_SHOW_SUCCESS, message);
        }

        if(message.contains("AddSuccess")){
            message = message.replace("AddSuccess->","");
            Log.i("DEBUG", message);
            callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_SHOW_SUCCESS, message);
        }

        if(message.contains("AddFailed")){
            callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_ADD_FAILED);
        }

        if(message.contains("SignOut")){
            state = START;
            callbackToManageKeyActivity(BluetoothControl.MANAGEKEY_SIGNOUT_SUCCESS);
        }
    }
}
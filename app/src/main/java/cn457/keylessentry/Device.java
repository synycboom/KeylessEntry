package cn457.keylessentry;

import android.bluetooth.BluetoothDevice;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class Device {
    private String address;
    private String deviceName;
    private boolean isChecked;
    private BluetoothDevice deviceObj;

    public Device(String address, String deviceName,BluetoothDevice deviceObj){
        this.address = address;
        this.deviceName = deviceName;
        this.deviceObj = deviceObj;
        isChecked = false;
    }
    public void setAddress(String address){
        this.address = address;
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    public boolean getIsChecked(){
        return isChecked;
    }
    public String getAddress(){
        return address;
    }
    public String getDeviceName(){
        return deviceName;
    }
    public BluetoothDevice getDeviceObj(){
        return deviceObj;
    }
}

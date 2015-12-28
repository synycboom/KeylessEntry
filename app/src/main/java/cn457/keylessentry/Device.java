package cn457.keylessentry;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class Device {
    private String address;
    private String deviceName;
    private boolean isChecked;

    public Device(String address, String deviceName){
        this.address = address;
        this.deviceName = deviceName;
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
}

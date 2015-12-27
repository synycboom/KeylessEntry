package cn457.keylessentry;

/**
 * Created by synycboom on 12/27/2015 AD.
 */
public class Device {
    private int id;
    private String deviceName;
    private boolean isChecked;

    public Device(int id, String deviceName){
        this.id = id;
        this.deviceName = deviceName;
        isChecked = false;
    }
    public void setId(int id){
        this.id = id;
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
    public int getId(){
        return id;
    }

    public String getDeviceName(){
        return deviceName;
    }
}

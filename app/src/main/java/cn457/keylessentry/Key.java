package cn457.keylessentry;

/**
 * Created by synycboom on 12/29/2015 AD.
 */
public class Key {
    private String key;
    private String name;
    private boolean isChecked;


    public Key(String key){
        this.key = key;
        isChecked = false;
    }

    public Key(String name, String key){
        this.key = key;
        this.name = name;
        isChecked = false;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    public boolean getIsChecked(){
        return isChecked;
    }
    public String getKey(){
        return key;
    }
    public String getName(){ return name; }
}

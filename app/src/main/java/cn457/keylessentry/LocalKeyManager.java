package cn457.keylessentry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by synycboom on 12/30/2015 AD.
 */
public class LocalKeyManager {
    private static final String PREF_NAME = "LOCAL_KEYS";
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private Context context;
    private Set<String> keys;
    private boolean isSetup;
    private static LocalKeyManager ourInstance = new LocalKeyManager();

    public static LocalKeyManager getInstance() {
        return ourInstance;
    }

    private LocalKeyManager() { isSetup = false; }

    private String getAllName(){
        return shared.getString("AllName", "%%^^&^&*(*^%#@$%$^&*(");
    }

    private void setAllName(String allName){
        editor.putString("AllName",allName);
        editor.commit();
    }

    public void setup(Context context){
        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            return;
        }

        this.context = context;
        keys = new HashSet<String>();
        shared = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = shared.edit();
    }

    public String getVal(String name){
        return shared.getString(name, "%%^^&^&*(*^%#@$%$^&*(");
    }

    public Set<String> getAll(){
        if(!getAllName().equals("%%^^&^&*(*^%#@$%$^&*(")){
            String _allName[] = getAllName().split(",");
            for(String name: _allName){
                keys.add(getVal(name));
            }
        }
        return keys;
    }

    public void setVal(String name,String val){
        if(!getAllName().equals("%%^^&^&*(*^%#@$%$^&*(")){
            boolean isEqual = false;
            String _allName[] = getAllName().split(",");
            for(String _name: _allName){
                if(name.equals(_name))
                    isEqual = true;
            }
            if(!isEqual)
                setAllName(getAllName() + "," + name);
        }
        else{
            setAllName("," + name);
        }
        editor.putString(name, val);
        editor.commit();
    }

    public void remove(String name){
        editor.putString(name, "%%^^&^&*(*^%#@$%$^&*(");
        editor.commit();
    }

    public void removeAll(){
        if(!getAllName().equals("%%^^&^&*(*^%#@$%$^&*(")){
            String _allName[] = getAllName().split(",");
            for(String name: _allName){
                editor.putString(name, "%%^^&^&*(*^%#@$%$^&*(");
                editor.commit();
            }
        }
    }

    public void select(String name){
        editor.putBoolean("IS_SELECT:" + name, true);
        editor.commit();
    }

    public boolean isSelected(String name){
        return shared.getBoolean("IS_SELECT:" + name, false);
    }
}

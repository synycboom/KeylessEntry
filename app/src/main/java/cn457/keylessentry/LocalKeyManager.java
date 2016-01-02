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
        return shared.getString("AllName", "");
    }

    private void setAllName(String allName){
        editor.putString("AllName",allName);
        editor.commit();
    }

    private void removeSomeName(String name){

        String allName = "";
        String _allName[] = getAllName().split(",");
        for(String _name: _allName){
            if(! _name.equals(name))
                allName += "," + _name;
        }

        allName = allName.substring(1,allName.length());
        Log.i("NAME",  allName);
        editor.putString("AllName",allName);
        editor.commit();
    }

    public void setup(Context context){
        isSetup = true;
        this.context = context;
        keys = new HashSet<String>();
        shared = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = shared.edit();
    }

    public boolean getIsSetup(){
        return isSetup;
    }

    public String getKey(String name){
        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        return shared.getString(name, "");
    }

    public Set<String> getAll(){

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        keys.clear();

        if(!getAllName().equals("")){
            Log.i("all name", getAllName());
            String _allName[] = getAllName().split(",");
            for(String name: _allName){
                if(!getKey(name).equals(""))
                    keys.add(name + ":" + getKey(name));
            }
        }
        return keys;
    }

    public void setKey(String name,String val){

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        if(!getAllName().equals("")){
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

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        editor.putString(name, "");
        editor.commit();

        removeSomeName(name);
    }

    public void removeAll(){

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        if(!getAllName().equals("")){
            String _allName[] = getAllName().split(",");
            for(String name: _allName){
                editor.putString(name, "");
                editor.commit();
            }
        }

        setAllName("");
    }

    public void select(String name){

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        editor.putBoolean("IS_SELECT:" + name, true);
        editor.commit();
    }

    public boolean isSelected(String name){

        if(!isSetup){
            Log.i("Warning", "Please setup to use");
            throw new AssertionError("You have to call setup method first");
        }

        return shared.getBoolean("IS_SELECT:" + name, false);
    }
}

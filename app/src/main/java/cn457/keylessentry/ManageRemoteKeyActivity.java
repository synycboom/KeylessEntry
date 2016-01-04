package cn457.keylessentry;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManageRemoteKeyActivity extends AppCompatActivity {

    private ListView mListView;
    private CustomKeyListAdapter mAdapter;
    private RelativeLayout background;
    private RelativeLayout addPanel;
    private LinearLayout threeButtonPanel;

    private Button addButton;
    private Button removeButton;
    private Button okToAddButton;
    private Button cancelToAddButton;
    private EditText keyEditText;
    private Button signOutButton;
    private Context context;
    List<Key> mKeys = new ArrayList<Key>();

    private final BroadcastReceiver mManageKeyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothControl.MANAGEKEY_ACTION)){
                Bundle extras = intent.getExtras();
                int result = extras.getInt(BluetoothControl.MANAGEKEY_RESULT);
                String keysResult = extras.getString(BluetoothControl.MANAGEKEY_KEYS);
                switch (result){
                    case BluetoothControl.MANAGEKEY_SHOW_SUCCESS:
                        Log.i("MANAGE", "Show Success");

                        String[] keys = keysResult.split(",");

                        for(String key : keys)
                            BluetoothControl.keys.add(new Key(key));

                        if(BluetoothControl.keys.isEmpty()){
                            Log.i("DEBUG", "Why it is empty??");
                            background.setVisibility(View.VISIBLE);
                            break;
                        }
                        background.setVisibility(View.INVISIBLE);
                        mKeys = new ArrayList<Key>(BluetoothControl.keys);
                        BluetoothControl.keys.clear();
                        showListViewOfDevices();
                        break;

                    case BluetoothControl.MANAGEKEY_SHOW_FAILED:
                        Log.i("MANAGE", "Show Failed");
                        break;
                    case BluetoothControl.MANAGEKEY_ADD_FAILED:
                        Log.i("MANAGE", "Add Failed");
                        Toast.makeText(getApplicationContext(),"Storage is full", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothControl.MANAGEKEY_REMOVE_SUCCESS:
                        Log.i("MANAGE", "Remove Success");
                        BluetoothControl.getInstance().getConnection().write("ShowKey");
                        break;
                    case BluetoothControl.MANAGEKEY_REMOVE_FAILED:
                        Log.i("MANAGE", "Remove Failed");
                        break;
                    case BluetoothControl.MANAGEKEY_SIGNOUT_SUCCESS:
                        Log.i("MANAGE", "Sign Out");
                        BluetoothControl.getInstance().resetConnection();
                        backToMainActivity();
                        break;
                }


            }
        }
    };

    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        backToMainActivity();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_remote_key);
        registerReceiver(mManageKeyReceiver, new IntentFilter(BluetoothControl.MANAGEKEY_ACTION));
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        addButton = (Button) findViewById(R.id.manage_key_add_button);
        removeButton = (Button) findViewById(R.id.manage_key_remove_button);
        signOutButton = (Button) findViewById(R.id.manage_key_signout_button);
        okToAddButton = (Button) findViewById(R.id.manage_key_add_ok_button);
        cancelToAddButton = (Button) findViewById(R.id.manage_key_add_cancel_button);
        keyEditText = (EditText) findViewById(R.id.key_text_box);
        background = (RelativeLayout) findViewById(R.id.manage_key_background);
        addPanel = (RelativeLayout) findViewById(R.id.manage_key_add_panel);
        threeButtonPanel = (LinearLayout) findViewById(R.id.manage_key_3_button_panel);
        background.setVisibility(View.INVISIBLE);
        addPanel.setVisibility(View.INVISIBLE);
        context = getApplicationContext();

        BluetoothControl.getInstance().getConnection().write("ShowKey");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Add Button","Show Add Button");
                keyEditText.setText("");
                addPanel.setVisibility(View.VISIBLE);
            }
        });

        okToAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (containWhiteSpace(keyEditText.getText().toString())) {
                    //show warning
                    return;
                }
                else if ( ! (keyEditText.getText().toString().matches(".*\\w.*"))) {
                    //show warning
                    return;
                }
                String newKey = "Add:" + keyEditText.getText().toString();
                Log.i("keyyyy", newKey);
                BluetoothControl.getInstance().getConnection().write(newKey);
                hideKeyboard();
                addPanel.setVisibility(View.INVISIBLE);
            }
        });

        cancelToAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                addPanel.setVisibility(View.INVISIBLE);
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothControl.getInstance().getConnection().write("SignOutRequest");
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getCheckedPosition().isEmpty()) {
                    Toast warning = Toast.makeText(getApplicationContext(), "Please select", Toast.LENGTH_SHORT);
                    warning.show();
                    return;
                }
                String all = "Remove:";
                for (int pos : mAdapter.getCheckedPosition()) {
                    all += mKeys.get(pos).getKey() + ",";
                }
                all = all.substring(0, all.length() - 1);
                Toast showRemove = Toast.makeText(getApplicationContext(), all, Toast.LENGTH_SHORT);
                showRemove.show();
                BluetoothControl.getInstance().getConnection().write(all);
            }
        });

    }

    @Override
    public void onBackPressed() {
        //prevent press back
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mManageKeyReceiver);
        unregisterReceiver(mBluetoothStateReceiver);
        BluetoothControl.getInstance().resetConnection();
    }

    private void showListViewOfDevices(){
        mListView = (ListView) findViewById(R.id.listview_remote_key);
        mAdapter = new CustomKeyListAdapter(this, mKeys, "remote");
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setChecked(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void backToMainActivity(){
        startActivity(new Intent(ManageRemoteKeyActivity.this, MainActivity.class));
    }

    private boolean containWhiteSpace(String text){
        boolean containsWhitespace = false;
        for (int i = 0; i < text.length() && !containsWhitespace; i++) {
            if (Character.isWhitespace(text.charAt(i))){
                containsWhitespace = true;
            }
        }
        return containsWhitespace;
    }

    private void hideKeyboard(){
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

package cn457.keylessentry;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UnlockModeActivity extends AppCompatActivity {

    private ListView mListView;
    private CustomKeyListAdapter mAdapter;
    private Context context;
    Intent service;
    List<Key> mKeys = new ArrayList<Key>();

    private Button startButton;
    private RelativeLayout background;

    private final BroadcastReceiver mEntryServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(EntryService.ENTRY_SERVICE_ACTION)) {
                Bundle extras = intent.getExtras();
                int state = extras.getInt(EntryService.ENTRY_SERVICE_RESULT);
                switch (state) {
                    case EntryService.ENTRY_SERVICE_START:
                        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
                        startButton.setText("Stop");
                        startButton.setBackgroundColor(0xFFFF0000);
                        break;
                    case EntryService.ENTRY_SERVICE_STOP:
                        startButton.setText("Start");
                        startButton.setBackgroundColor(0xFF00FF00);
                        Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_mode);

        startButton = (Button) findViewById(R.id.unlock_start_button);
        background = (RelativeLayout) findViewById(R.id.unlock_mode_background);

        registerReceiver(mEntryServiceReceiver, new IntentFilter(EntryService.ENTRY_SERVICE_ACTION));
        context = getApplicationContext();

        if( ! LocalKeyManager.getInstance().getIsSetup() ){
            LocalKeyManager.getInstance().setup(context);
        }

        synchronized (LocalKeyManager.getInstance()){
            showListViewOfDevices();
        }

        if(isMyServiceRunning(EntryService.class)){
            startButton.setText("Stop");
            startButton.setBackgroundColor(0xFFFF0000);
        }
        else{
            startButton.setText("Start");
            startButton.setBackgroundColor(0xFF00FF00);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service = new Intent(UnlockModeActivity.this, EntryService.class);
                if(!BluetoothControl.getInstance().getAdapter().isEnabled()){
                    Intent intent = new Intent(UnlockModeActivity.this, MainActivity.class);
                    startActivity(intent);

                    Toast prevent =  Toast.makeText(UnlockModeActivity.this,"Please turn on bluetooth", Toast.LENGTH_SHORT);
                    prevent.show();
                }
                else if(isMyServiceRunning(EntryService.class)){
                    stopService(service);

                }else{
                    startService(service);
                }
            }
        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mEntryServiceReceiver);
    }

    private boolean setupListView(){
        Set<String> tmp =  LocalKeyManager.getInstance().getAll();

        Log.i("SIZE", tmp.size() + "");

        if(tmp.isEmpty())
            return false;

        mKeys.clear();
        for(String pair : tmp){
            Log.i("DATA", pair);
            String p[] = pair.split(":");
            mKeys.add(new Key(p[0],p[1]));
        }

        return true;
    }

    private void showListViewOfDevices(){
        mListView = (ListView) findViewById(R.id.listview_unlock_key);

        if( !setupListView()){
            background.setVisibility(View.VISIBLE);
            return;
        }

        background.setVisibility(View.INVISIBLE);
        mAdapter = new CustomKeyListAdapter(this, mKeys, "unlock");
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setChecked(position);
                mAdapter.setSelected(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

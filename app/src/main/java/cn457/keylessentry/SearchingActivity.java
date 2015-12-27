package cn457.keylessentry;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchingActivity extends Activity {

    private ListView mListView;
    private CustomAdapter mAdapter;

    List<Device> mDevices = Arrays.asList(
            new Device(1, "Android"),
            new Device(2, "iPhone"),
            new Device(3, "WindowsMobile"),
            new Device(4, "Blackberry"),
            new Device(5, "WebOS"),
            new Device(6, "Ubuntu"),
            new Device(7, "Windows7"),
            new Device(8, "Max OS X"),
            new Device(9, "Linux"),
            new Device(10, "OS/2"),
            new Device(11, "Ubuntu")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new CustomAdapter(this, mDevices);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.i("testy", "I Clicked on Row " + position + " and it worked!");
                mAdapter.setChecked(position);
                mAdapter.notifyDataSetChanged();
            }
        });


    }
}

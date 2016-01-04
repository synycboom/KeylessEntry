package cn457.keylessentry;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ManageLocalKeyActivity extends AppCompatActivity {

    private ListView mListView;
    private CustomKeyListAdapter mAdapter;
    private Context context;
    List<Key> mKeys = new ArrayList<Key>();

    private Button addButton;
    private Button removeButton;
    private Button okToAddButton;
    private Button cancelToAddButton;
    private EditText keyEditText;
    private EditText nameEditText;
    private RelativeLayout background;
    private RelativeLayout addPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_local_key);

        addButton = (Button) findViewById(R.id.local_add_button);
        removeButton = (Button) findViewById(R.id.local_remove_button);
        okToAddButton = (Button) findViewById(R.id.manage_local_key_add_ok_button);
        cancelToAddButton = (Button) findViewById(R.id.manage_local_key_add_cancel_button);
        keyEditText = (EditText) findViewById(R.id.local_key_text_box);
        nameEditText = (EditText) findViewById(R.id.local_name_text_box);
        background = (RelativeLayout) findViewById(R.id.manage_local_key_background);
        addPanel = (RelativeLayout) findViewById(R.id.manage_local_key_add_panel);


        addPanel.setVisibility(View.INVISIBLE);
        context = getApplicationContext();

        if( ! LocalKeyManager.getInstance().getIsSetup() ){
            LocalKeyManager.getInstance().setup(context);
        }

        synchronized (LocalKeyManager.getInstance()){
            showListViewOfDevices();
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Button","Add click");
                keyEditText.setText("");
                nameEditText.setText("");
                addPanel.setVisibility(View.VISIBLE);
            }
        });

        okToAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (containWhiteSpace(keyEditText.getText().toString()) ||
                        containWhiteSpace(nameEditText.getText().toString())) {
                    //show warning
                    return;
                }
                else if ( ! (keyEditText.getText().toString().matches(".*\\w.*") ||
                        nameEditText.getText().toString().matches(".*\\w.*"))) {
                    //show warning
                    return;
                }else {
                    String newKey = keyEditText.getText().toString();
                    String newName = nameEditText.getText().toString();

                    LocalKeyManager.getInstance().setKey(newName, newKey);
                    Toast.makeText(getApplicationContext(), "Key added", Toast.LENGTH_SHORT).show();
                    addPanel.setVisibility(View.INVISIBLE);

                    hideKeyboard();

                    synchronized (LocalKeyManager.getInstance()) {
                        showListViewOfDevices();
                    }
                }
            }
        });

        cancelToAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                addPanel.setVisibility(View.INVISIBLE);
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

                List<Integer> posToDelete = new ArrayList<Integer>();
                for (int pos : mAdapter.getCheckedPosition()) {
                    //TODO remove local keys
                    LocalKeyManager.getInstance().remove(mKeys.get(pos).getName());
                    posToDelete.add(pos);
                }

                //sort poToDelete in descending order to remove key with no effect to index
                Collections.sort(posToDelete, Collections.reverseOrder());
                for (int pos : posToDelete) {
                    mKeys.remove(pos);
                }

                Toast showRemove = Toast.makeText(getApplicationContext(), "Removed selected", Toast.LENGTH_SHORT);
                showRemove.show();

                showListViewOfDevices();
            }
        });
    }

    private boolean setupListView(){
        Set<String> tmp =  LocalKeyManager.getInstance().getAll();

        Log.i("SIZE", tmp.size()+"");

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
        mListView = (ListView) findViewById(R.id.listview_local_key);

        if( !setupListView()){
            background.setVisibility(View.VISIBLE);
            return;
        }

        background.setVisibility(View.INVISIBLE);
        mAdapter = new CustomKeyListAdapter(this, mKeys, "local");
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.setChecked(position);
                mAdapter.notifyDataSetChanged();
            }
        });
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


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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AuthenticationActivity extends AppCompatActivity {


    private final BroadcastReceiver mAuthenticationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothControl.AUTHENTICATION_ACTION)){
                Bundle extras = intent.getExtras();
                int result = extras.getInt(BluetoothControl.AUTHENTICATION_RESULT);
                TextView resultText = (TextView) findViewById(R.id.authen_result_text);
                switch (result){
                    case BluetoothControl.AUTHENTICATION_SUCCESS:
                        Log.i("AUTHEN", "Success");
                        resultText.setText("Sign in Success");
                        resultText.setTextColor(0xCC0000);
                        goToManageKeyActivity();
                        break;
                    case BluetoothControl.AUTHENTICATION_FAILED:
                        Log.i("AUTHEN", "Failed");
                        resultText.setText("Sign in Failed");
                        resultText.setTextColor(0x00CC00);
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
        setContentView(R.layout.activity_authentication);
        registerReceiver(mAuthenticationReceiver, new IntentFilter(BluetoothControl.AUTHENTICATION_ACTION));
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        Button okButton = (Button) findViewById(R.id.authen_ok_button);
        Button cancelButton = (Button) findViewById(R.id.authen_cancel_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText passwordText = (EditText) findViewById(R.id.authen_password_textbox);
                String password = passwordText.getText().toString();
                BluetoothControl.getInstance().getConnection().write(password);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToSearchingActivity();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mAuthenticationReceiver);
        unregisterReceiver(mBluetoothStateReceiver);
    }

    private void backToSearchingActivity(){
        startActivity(new Intent(AuthenticationActivity.this, SearchingActivity.class));
    }

    private void backToMainActivity(){
        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
    }

    private void goToManageKeyActivity(){
        startActivity(new Intent(AuthenticationActivity.this, ManageKeyActivity.class));
    }

}

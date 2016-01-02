package cn457.keylessentry;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import cn457.keylessentry.FragmentPack.GetKeyDialogFragment;
import cn457.keylessentry.FragmentPack.SelectKeyDialogFragment;

public class SharingActivity extends AppCompatActivity {

    public Context context = SharingActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);


        Button sendButton = (Button) findViewById(R.id.send_key_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSelectKeyDialog();
            }
        });

        Button getButton = (Button) findViewById(R.id.get_key_button);
        getButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showGetDialog();
            }
        });

    }

    public void showGetDialog() {
        FragmentManager fm = getSupportFragmentManager();
        GetKeyDialogFragment getKeyDialog = GetKeyDialogFragment.newInstance(context);
//        GetKeyDialogFragment getDialog = new GetKeyDialogFragment();
        getKeyDialog.show(fm, "SEND DIALOG");
    }

    private void showSelectKeyDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SelectKeyDialogFragment selectkeyDialog = SelectKeyDialogFragment.newInstance(context);
//        SelectKeyDialogFragment selectkeyDialog = new SelectKeyDialogFragment();
        selectkeyDialog.show(fm,"SELECT KEY");
    }

}

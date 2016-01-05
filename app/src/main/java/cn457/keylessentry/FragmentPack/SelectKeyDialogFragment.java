package cn457.keylessentry.FragmentPack;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.squareup.okhttp.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn457.keylessentry.LocalKeyManager;
import cn457.keylessentry.SharingActivity;
import cn457.keylessentry.api.KeyObject;
import cn457.keylessentry.api.PinObject;
import cn457.keylessentry.api.SendingKeyApi;
import cn457.keylessentry.api.ServiceGenerator;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by tnpxu on 30/12/2558.
 */
public class SelectKeyDialogFragment extends DialogFragment{

    private EditText mEditText;
    private  ArrayList mSelectedItems;
    private static Context actContext;

    public SelectKeyDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static SelectKeyDialogFragment newInstance(Context context) {
        SelectKeyDialogFragment frag = new SelectKeyDialogFragment();
        Bundle args = new Bundle();
        actContext = context;
        return frag;
    }

    public Object setKeys[];

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList();  // Where we track the selected items
//        final String[] listKeys = new String[]{"sss","sdfdsf","asdasd"};
        //String[] listKeys = new String[keyLength];

        LocalKeyManager.getInstance().setup(getContext());

        synchronized (LocalKeyManager.getInstance()){
            setKeys = LocalKeyManager.getInstance().getAll().toArray();
        }

        String[] listKeys = new String[setKeys.length];

        int count = 0;
        for(Object splitKey : setKeys) {
            String tmp[] = ((String)splitKey).split(":");
            listKeys[count] = tmp[0];
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle("Select key to send")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(listKeys, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSelectedItems.clear();
                                mSelectedItems.add(setKeys[which]);

                            }
                        })
                        // Set the action buttons
                .setPositiveButton("send", new DialogInterface.OnClickListener() {
                    ProgressDialog pDialog;
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // Must tick then call api
                        if(!(mSelectedItems.isEmpty())) {

                            pDialog = ProgressDialog.show(actContext, "Sending", "Please wait");

                            SendingKeyApi service = ServiceGenerator.createService(SendingKeyApi.class);

                            //if didn't tick close dialog


                            //sendKeyValue
                            String keyValue = (String)mSelectedItems.get(0);
                            KeyObject obj = new KeyObject(keyValue);

                            Call<KeyObject> call = service.sending(obj);
                            call.enqueue(new Callback<KeyObject>() {
                                @Override
                                public void onResponse(Response<KeyObject> response, Retrofit retrofit) {
                                    Log.v("Upload", "success");
                                    pDialog.dismiss();
                                    KeyObject res = response.body();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(actContext);
                                    builder.setTitle("Your Key PIN").setMessage(res.pin)
                                            .setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                }
                                            });
                                    builder.create().show();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Log.e("Upload", t.getMessage());
                                    pDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(actContext);
                                    builder.setTitle("Failed").setMessage("Please send key again")
                                            .setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                }
                                            });
                                    builder.create().show();
                                }
                            });

                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSelectedItems.clear();
                    }
                });

        return builder.create();
    }
}

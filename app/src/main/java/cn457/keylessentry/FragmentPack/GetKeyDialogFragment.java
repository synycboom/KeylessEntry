package cn457.keylessentry.FragmentPack;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.View;

import cn457.keylessentry.R;
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
public class GetKeyDialogFragment extends DialogFragment {

    private static Context actContext;
    public EditText mEdit;

    public GetKeyDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static GetKeyDialogFragment newInstance(Context context) {
        GetKeyDialogFragment frag = new GetKeyDialogFragment();
        Bundle args = new Bundle();
        actContext = context;
        return frag;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.get_dialog, container, false);
//        mEdit = (EditText) view.findViewById(R.id.pin);
//
//        return view;
//    }

//    public void onActivityCreated(Bundle savedInstanceState)
//    {
//        View view = getView();
//
//        mEdit = (EditText) view.findViewById(R.id.pin);
//
//        super.onActivityCreated(savedInstanceState);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.get_dialog, null);
        builder.setView(view).setTitle("GET KEY")
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    ProgressDialog pDialog;
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        mEdit = (EditText) view.findViewById(R.id.pin);
                        pDialog = ProgressDialog.show(actContext, "Sending", "Please wait");

                        SendingKeyApi service = ServiceGenerator.createService(SendingKeyApi.class);
                        PinObject obj = new PinObject(mEdit.getText().toString());
                        Call<PinObject> call = service.sendingPin(obj);
                        call.enqueue(new Callback<PinObject>() {
                            @Override
                            public void onResponse(Response<PinObject> response, Retrofit retrofit) {
                                Log.v("Upload", "success");
                                pDialog.dismiss();
                                PinObject obj = response.body();
                                final boolean validKey;

                                String title;
                                String message;
                                if(obj.msg.equals("valid")) {
                                    title = "Found Your Key!!!!";
                                    message = "Key value:" + obj.key;
                                    validKey = true;
                                } else {
                                    title = "Invalid PIN";
                                    message = "Key value:" + obj.key;
                                    validKey = false;
                                }

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(title).setMessage(message)
                                        .setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                if(validKey) {
                                                    /////// STORE KEY !!!!!!!!!!! ///////
                                                }
                                            }
                                        });
                                builder.create().show();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Log.e("Upload", t.getMessage());
                                pDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Failed").setMessage("Please send pin again")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                builder.create().show();
                            }
                        });
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GetKeyDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}

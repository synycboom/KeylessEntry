package cn457.keylessentry.api;

import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;

/**
 * Created by tnpxu on 11/12/2558.
 */

/***
 String description = "Hello, Team alpha this is Drone eiei";
 SendingPhotoApi service = ServiceGenerator.createService(SendingPhotoApi.class);
 RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), photo);

 Call<String> call = service.upload(requestBody, description);
 call.enqueue(new Callback<String>() {
@Override
public void onResponse(Response<String> response, Retrofit retrofit) {
Log.v("Upload", "success");
pDialog.dismiss();
}

@Override
public void onFailure(Throwable t) {
Log.e("Upload", t.getMessage());
pDialog.dismiss();
}
});
 ***/


public interface SendingKeyApi {

    @POST("/api/sendkey")
    Call<KeyObject> sending(@Body KeyObject keyOb);

    @POST("/api/sendpin")
    Call<PinObject> sendingPin(@Body PinObject pinOb);
}

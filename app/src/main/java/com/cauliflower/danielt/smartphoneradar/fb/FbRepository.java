package com.cauliflower.danielt.smartphoneradar.fb;

import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.gson.Gson;

public class FbRepository {

    public FbRepository() {
    }

    public MutableLiveData<String> getEmail(AccessToken accessToken) {
        MutableLiveData<String> email = new MutableLiveData<>();
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                (object, response) -> {
                    email.setValue(
                            new Gson().fromJson(object.toString(), EmailResponse.class).getEmail());
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
        return email;
    }


}

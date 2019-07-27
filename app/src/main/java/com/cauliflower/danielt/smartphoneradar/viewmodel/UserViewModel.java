package com.cauliflower.danielt.smartphoneradar.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.fb.FbRepository;
import com.cauliflower.danielt.smartphoneradar.RadarApp;
import com.cauliflower.danielt.smartphoneradar.data.DataRepository;
import com.cauliflower.danielt.smartphoneradar.data.RadarUser;
import com.facebook.AccessToken;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private DataRepository mRepository;
    private MediatorLiveData<List<RadarUser>> targetList;

    private MutableLiveData<String> mEmail;
    private FbRepository mFbRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        RadarApp app = ((RadarApp) application);
        mRepository = app.getRepository();
        mFbRepository = app.getFbRepository();
        targetList = new MediatorLiveData<>();
        targetList.addSource(mRepository.getTargets(),
                radarUsers -> targetList.setValue(radarUsers));

        mEmail = new MutableLiveData<>();
        if (AccessToken.isCurrentAccessTokenActive()) {
            mEmail = mFbRepository.getEmail(AccessToken.getCurrentAccessToken());
        }
    }

    public MutableLiveData<String> emailFromFb(){
        return mEmail;
    }

    public void insertUsers(RadarUser... users) {
        mRepository.insertUsers(users);
    }

    public LiveData<RadarUser> getUser(String email) {
        return mRepository.getUser(email);
    }

    public LiveData<List<RadarUser>> getTargets() {
        return targetList;
    }

    public List<RadarUser> getTargetsSync() {
        return mRepository.getTargetsSync();
    }

    public void updateUsers(RadarUser... users) {
        mRepository.updateUsers(users);
    }

    public void deleteUsers(RadarUser... users) {
        mRepository.deleteUsers(users);
    }
}

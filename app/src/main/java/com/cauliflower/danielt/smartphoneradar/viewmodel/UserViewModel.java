package com.cauliflower.danielt.smartphoneradar.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.RadarApp;
import com.cauliflower.danielt.smartphoneradar.data.DataRepository;
import com.cauliflower.danielt.smartphoneradar.data.RadarUser;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private DataRepository mRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        RadarApp app = ((RadarApp) application);
        mRepository = app.getRepository();
    }

    public void insertUsers(RadarUser... users) {
        mRepository.insertUsers(users);
    }

    public LiveData<RadarUser> getUser(String email) {
        return mRepository.getUser(email);
    }

    public LiveData<List<RadarUser>> getTargetsTracked() {
        return mRepository.getTargetsTracked();
    }

    public void updateUsers(RadarUser... users) {
        mRepository.updateUsers(users);
    }

    public void deleteUsers(RadarUser... users) {
        mRepository.deleteUsers(users);
    }
}

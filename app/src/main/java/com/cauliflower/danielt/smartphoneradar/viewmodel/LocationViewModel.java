package com.cauliflower.danielt.smartphoneradar.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.RadarApp;
import com.cauliflower.danielt.smartphoneradar.data.DataRepository;
import com.cauliflower.danielt.smartphoneradar.data.RadarLocation;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {
    private DataRepository mDataRepository;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        this.mDataRepository = ((RadarApp) application).getRepository();
    }

    public void insertLocations(RadarLocation... locations) {
        mDataRepository.insertLocations(locations);
    }

    public LiveData<List<RadarLocation>> getLocations(String email) {
        return mDataRepository.getLocations(email);
    }

    //可能用不到～因為 getLocations 就夠用了
    /*public RadarLocation getLatestLocation() {
        return mDataRepository.getLatestLocation(mEmail);
    }*/

    public void deleteLocations(RadarLocation... locations) {
        mDataRepository.deleteLocations(locations);
    }

    /*static class Factory extends ViewModelProvider.NewInstanceFactory {

        private String mEmail;
        private DataRepository mRepository;

        public Factory(@NonNull Application application, @NonNull String email) {
            this.mEmail = email;
            this.mRepository = ((RadarApp) application).getRepository();
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(DataRepository.class, String.class).newInstance(mEmail, mRepository);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/
}

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
    private String mEmail;
    private MediatorLiveData<List<RadarLocation>> observableData;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        this.mDataRepository = ((RadarApp) application).getRepository();
        /*this.mEmail = email;*/
        observableData = new MediatorLiveData<>();
    }

    //If you start to track a target or change the target, call this method.
    public void loadLocations(String email) {
        mEmail = email;
        observableData.addSource(getLocations(),
                radarLocations -> observableData.postValue(radarLocations));
    }

    public void insertLocations(RadarLocation... locations) {
        mDataRepository.insertLocations(locations);
    }

    private LiveData<List<RadarLocation>> getLocations() {
        return mDataRepository.getLocations(mEmail);
    }

    //可能用不到～因為 getLocations 就夠用了
    public RadarLocation getLatestLocation() {
        return mDataRepository.getLatestLocation(mEmail);
    }

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

package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class DataRepository {

    private RadarDatabase mDatabase;
    private Executor executor;
    private static DataRepository sInstance;

    private DataRepository(RadarDatabase database, ExecutorService executorService) {
        this.mDatabase = database;
        this.executor = executorService;
    }

    public static DataRepository getInstance(RadarDatabase database, ExecutorService executorService) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database, executorService);
                }
            }
        }
        return sInstance;
    }

    //RadarUser
    public void insertUsers(RadarUser... users) {
        executor.execute(() -> mDatabase.radarUserDao().insertUsers(users));
    }

    public LiveData<List<RadarUser>> getTargetsTracked() {
        return mDatabase.radarUserDao().getUsers(RadarContract.UserEntry.USED_FOR_GETLOCATION);
    }

    public LiveData<RadarUser> getUser(String email) {
        return mDatabase.radarUserDao().getUser(email);
    }

    public void updateUsers(RadarUser... users) {
        executor.execute(() -> mDatabase.radarUserDao().updateUsers(users));
    }

    public void deleteUsers(RadarUser... users) {
        executor.execute(() -> mDatabase.radarUserDao().deleteUsers(users));
    }

    //RadarLocation
    public void insertLocations(RadarLocation... locations) {
        executor.execute(() -> mDatabase.radarLocationDao().insertLocations(locations));
    }

    public LiveData<List<RadarLocation>> getLocations(String email) {
        return mDatabase.radarLocationDao().getLocations(email);
    }

    //或許這個方法可以被 getLocations(String email) 取代
    public RadarLocation getLatestLocation(String email) {
        return mDatabase.radarLocationDao().getLatestLocation(email);
    }

    public void deleteLocations(RadarLocation... locations) {
        executor.execute(() -> mDatabase.radarLocationDao().deleteLocations(locations));
    }

}

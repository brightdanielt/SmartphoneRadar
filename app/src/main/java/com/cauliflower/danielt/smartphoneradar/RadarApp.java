package com.cauliflower.danielt.smartphoneradar;

import android.app.Application;

import com.cauliflower.danielt.smartphoneradar.data.DataRepository;
import com.cauliflower.danielt.smartphoneradar.data.RadarDatabase;
import com.cauliflower.danielt.smartphoneradar.fb.FbRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RadarApp extends Application {

    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
         executorService = Executors.newSingleThreadExecutor();
    }

    public RadarDatabase getDatabase() {
        return RadarDatabase.getInstance(this);
    }

    public DataRepository getRepository(){
        return DataRepository.getInstance(getDatabase(),executorService);
    }

    public FbRepository getFbRepository(){
        return new FbRepository();
    }

}

package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RadarLocationDao {

    @Query("SELECT * FROM location WHERE email = :email")
    LiveData<List<RadarLocation>> getLocations(String email);

    //todo !!!發現疑點，原本想依照 timestamp 排序，但看起來這邊排序對象是經過 typeConverter 的 String
    @Query("SELECT * FROM location WHERE email = :email ORDER BY time DESC LIMIT 1")
    RadarLocation getLatestLocation(String email);

    @Insert
    void insertLocation(RadarLocation... locations);

    //位置是已發生的事實，不大可能要更新這筆位置，所以註解掉
    /*@Update
    void update();*/

    @Delete
    void deleteLocation();
}

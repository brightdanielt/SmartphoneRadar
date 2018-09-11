package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RadarUserDao {

    @Query("SELECT * FROM user")
    LiveData<List<RadarUser>> getAllUsers();

    @Query("SELECT * FROM user WHERE email = :email")
    LiveData<RadarUser> getUser(String email);

    @Query("SELECT * FROM user WHERE userFor = :usedFor")
    LiveData<List<RadarUser>> getUsers(String usedFor);

    @Query("SELECT * FROM user WHERE userFor = :usedFor")
    List<RadarUser> getUsersSync(String usedFor);

    //回傳值若依照 SQLite 的規則，新增失敗應該回傳 -1
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long[] insertUsers(RadarUser... users);

    @Update
    void updateUsers(RadarUser... users);

    @Delete
    void deleteUsers(RadarUser... users);
}

package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface RadarUserDao {

    @Query("SELECT * FROM user")
    void getAllUsers();

    @Query("SELECT * FROM user WHERE email = :email")
    void getUser(String email);

    //回傳值若依照 SQLite 的規則，新增失敗應該回傳 -1
    @Insert(onConflict = OnConflictStrategy.ABORT)
    int insertUsers(RadarUser... users);

    @Update
    void updateUsers(RadarUser... users);

    @Delete
    void deleteUsers(RadarUser... users);
}

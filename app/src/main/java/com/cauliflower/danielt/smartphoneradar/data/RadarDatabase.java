package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.DATABASE_NAME;

@Database(entities = {RadarUser.class, RadarLocation.class}, version = 1)
public abstract class RadarDatabase extends RoomDatabase {
    private static RadarDatabase sInstance;

    public abstract RadarUserDao radarUserDao();

    public abstract RadarLocationDao radarLocationDao();

    public static RadarDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (RadarDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private static RadarDatabase buildDatabase(Context applicationContext) {
        return Room.databaseBuilder(applicationContext, RadarDatabase.class, DATABASE_NAME)
                /*.allowMainThreadQueries()*/
                //終於不用製造假資料了！通常假資料會在 addCallback 時，加入資料庫
                /*.addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                    }
                })*/
                .build();
    }

}

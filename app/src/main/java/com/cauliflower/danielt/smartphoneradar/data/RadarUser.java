package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.COLUMN_USER_EMAIL;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.COLUMN_USER_IN_USE;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.COLUMN_USER_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.COLUMN_USER_USED_FOR;
import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry.TABLE_USER;

@Entity(tableName = TABLE_USER)
public class RadarUser {

    //過去允許追蹤自己的位置，是為了測試方便，現在要禁止了，所以 PrimaryKey 只有 email
    @PrimaryKey
    @ColumnInfo(name = COLUMN_USER_EMAIL)
    private String email;

    @ColumnInfo(name = COLUMN_USER_PASSWORD)
    private String password;

    @ColumnInfo(name = COLUMN_USER_USED_FOR)
    private String usedFor;

    @ColumnInfo(name = COLUMN_USER_IN_USE)
    private boolean inUse;

    public RadarUser() {
    }

    public RadarUser(String email, String password, String usedFor, boolean inUse) {
        this.email = email;
        this.password = password;
        this.usedFor = usedFor;
        this.inUse = inUse;
    }

    public RadarUser(@NonNull Cursor cursor) {
        int index_email = cursor.getColumnIndex(COLUMN_USER_EMAIL);
        int index_password = cursor.getColumnIndex(COLUMN_USER_PASSWORD);
        int index_usedFor = cursor.getColumnIndex(COLUMN_USER_USED_FOR);
        int index_in_use = cursor.getColumnIndex(COLUMN_USER_IN_USE);

        email = cursor.getString(index_email);
        password = cursor.getString(index_password);
        usedFor = cursor.getString(index_usedFor);
        inUse = Boolean.getBoolean(cursor.getString(index_in_use));
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsedFor(String usedFor) {
        this.usedFor = usedFor;
    }

    public void setInUse(boolean in_use) {
        this.inUse = in_use;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public String getUsedFor() {
        return usedFor;
    }

    public boolean getInUse() {
        return inUse;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_USED_FOR, usedFor);
        values.put(COLUMN_USER_IN_USE, inUse);
        return values;
    }

    @Override
    public String toString() {
        return "RadarUser" + "\t" +
                email + "\t" +
                password + "\t" +
                usedFor + "\t" +
                inUse;
    }
}

package com.cauliflower.danielt.smartphoneradar.obj;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.cauliflower.danielt.smartphoneradar.data.RadarContract;

public class RadarUser {
    private String email;
    private String password;
    private String usedFor;
    private String in_use;

    public RadarUser() {
    }

    public RadarUser(String email, String password, String usedFor, String in_use) {
        this.email = email;
        this.password = password;
        this.usedFor = usedFor;
        this.in_use = in_use;
    }

    public RadarUser(@NonNull Cursor cursor) {
        int index_email = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_EMAIL);
        int index_password = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_PASSWORD);
        int index_usedFor = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_USED_FOR);
        int index_in_use = cursor.getColumnIndex(RadarContract.UserEntry.COLUMN_USER_IN_USE);

        email = cursor.getString(index_email);
        password = cursor.getString(index_password);
        usedFor = cursor.getString(index_usedFor);
        in_use = cursor.getString(index_in_use);
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

    public void setIn_use(String in_use) {
        this.in_use = in_use;
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

    public String getIn_use() {
        return in_use;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(RadarContract.UserEntry.COLUMN_USER_EMAIL, email);
        values.put(RadarContract.UserEntry.COLUMN_USER_PASSWORD, password);
        values.put(RadarContract.UserEntry.COLUMN_USER_USED_FOR, usedFor);
        values.put(RadarContract.UserEntry.COLUMN_USER_IN_USE, in_use);
        return values;
    }

    @Override
    public String toString() {
        return "RadarUser" + "\t" +
                email + "\t" +
                password + "\t" +
                usedFor + "\t" +
                in_use;
    }
}

package com.cauliflower.danielt.smartphoneradar.data;

import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import java.text.DateFormat;
import java.util.Date;

public class RoomConverter {
    @TypeConverter
    public String timestampToString(long timestamp) {
        final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        final Date date = new Date(timestamp);
        return format.format(date);
    }

    @TypeConverter
    public long dateToTimestamp(Date date) {
        return date.getTime();
    }
}

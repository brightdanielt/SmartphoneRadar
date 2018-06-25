package com.cauliflower.danielt.smartphoneradar.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class RadarContract {

    public static final String CONTENT_AUTHORITY = "com.cauliflower.danielt.smartphoneradar";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for SmartphoneRadar.
     * */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USER = "user";
    public static final String PATH_LOCATION = "location";


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private RadarContract() {
    }

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_USER)
                .build();

        /**
         * Name of database table for user
         */
        public static final String TABLE_USER = "user";

        /**
         * Unique ID number for the user (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Account of the user.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_ACCOUNT = "account";

        /**
         * Password of the user.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_PASSWORD = "password";

        /**
         * To Tell it's a sendLocation use or getLocation user.
         * <p>
         * The only possible value are {@link #USED_FOR_SENDLOCATION},{@link #USED_FOR_GETLOCATION}
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_USED_FOR = "userFor";

        /**
         * To tell if the user is using.
         * <p>
         * The only possible value are {@link #IN_USE_YES},{@link #IN_USE_NO}
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_IN_USE = "inUse";

        /**
         * Possible values for the usedFor of the user.
         */
        public static final String USED_FOR_SENDLOCATION = "usedFor_sendLocation";
        public static final String USED_FOR_GETLOCATION = "usedFor_getLocation";

        /**
         * Possible values for the inUse of the user.
         */
        public static final String IN_USE_YES = "in_use_yes";
        public static final String IN_USE_NO = "in_use_no";

    }

    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LOCATION)
                .build();

        /**
         * Name of database table for user
         */
        public static final String TABLE_LOCATION = "location";

        /**
         * Unique ID number for the location (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * The account who is in the location.
         * This column is related with {@link UserEntry#COLUMN_USER_ACCOUNT}
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_LOCATION_ACCOUNT = "account";

        /**
         * Time of the location.
         * <p>
         * Type: DATETIME
         */
        public final static String COLUMN_LOCATION_TIME = "time";

        /**
         * Latitude of the location.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_LOCATION_LATITUDE = "latitude";

        /**
         * Longitude of the location.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_LOCATION_LONGITUDE = "longitude";

    }
}

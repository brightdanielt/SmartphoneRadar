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

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI;s that SmartphoneRadar
     * can handle. For instance,
     *     content://com.cauliflower.danielt.smartphoneradar/user/
     *     [            BASE_CONTENT_URI                   ][ PATH_USER ]
     * is a valid path for looking at user data.
     *
     *     content://com.cauliflower.danielt.smartphoneradar/usedForgetLocation/
     *
     * will failed ,as the ContentProvider hasn't been given any information on what to do with
     * "usedForgetLocation". At least, let's hope not.Don't be that dev. reader.
     * */
    public static final String PATH_USER = "user";
    public static final String PATH_LOCATION = "location";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private RadarContract() {
    }

    /**
     * Inner class that define the table content of the user table
     */
    public static final class UserEntry implements BaseColumns {

        /**
         * The CONTENT_URI used to query,update,insert the user table from the {@link RadarProvider}
         */
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
         * Email of the user.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_EMAIL = "email";

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

    /**
     * Inner class that define the table content of the user table
     */
    public static final class LocationEntry implements BaseColumns {

        /**
         * The CONTENT_URI used to query,update,insert the location table from the {@link RadarProvider}
         */
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
         * This column is related with {@link UserEntry#COLUMN_USER_EMAIL}
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_LOCATION_EMAIL = "email";

        /**
         * Time of the location.
         * <p>
         * Type: TEXT
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

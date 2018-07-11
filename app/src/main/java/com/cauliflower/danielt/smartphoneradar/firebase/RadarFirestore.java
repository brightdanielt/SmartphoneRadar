package com.cauliflower.danielt.smartphoneradar.firebase;

import com.google.firebase.auth.FirebaseUser;

public class RadarFirestore {

    /**
     * In firestore, an user is composed by email and password.
     * <p>
     * After sign in facebook ,we create the user to firestore.
     * Beside, firestore security rule will verify {@link FirebaseUser#getUid()},
     * if the user didn't sign in, this create will failed.
     *
     * @param email    Email get from {@link FirebaseUser#getEmail()}.
     * @param password User's customized password.
     */
    public static void createUser(String email, String password) {

    }

    /**
     * Before update password ,firestore security rule will verify
     * if the original password is equal to password in firebase,
     * if the verification passed ,the user can update the password.
     *
     * @param email            Email get from {@link FirebaseUser#getEmail()}.
     * @param originalPassword The original password for verification.
     * @param newPassword      The new customized password.
     */
    public static void updatePassword(String email, String originalPassword, String newPassword) {

    }

    /**
     * Before update location ,firestore security rule will verify
     * if the imei of the device is equal to imei in firebase,
     * if the verification passed ,the user can update the location.
     *
     * @param email     Email get from {@link FirebaseUser#getEmail()}.
     * @param imei      The imei for verification.
     * @param latitude  The latitude of the device.
     * @param longitude The longitude of the device.
     */
    public static void updateLocation(String email, String imei, double latitude, double longitude) {

    }

    /**
     * This method is testing.
     * The user don't have to sign in facebook to query new location.
     * <p>
     * Before setting listener to location ,firestore security rule will verify
     * if the password is equal to password in firebase,
     * if the verification passed ,the user can set listener to the location.
     *
     * @param email           The email that user sign in facebook.
     * @param password        The user's password for verification.
     * @param coordinateIndex There are 5 temp coordinate to listen.
     */
    public static void setOnLocationUpdateListener(String email, String password, String coordinateIndex) {

    }

    /**
     * The users don't have to sign in facebook to verify themselves.
     * <p>
     * A user can track location of multiple users so the user could has a user list to
     * choose which user to track, before adding a user to the list, you should prove that
     * you have the right to access the user.
     * <p>
     * Firestore security rule will verify if the password is equal to password in firebase,
     * if the verification passed ,the user has the right to read this user.
     *
     * @param email    The email that user sign in facebook.
     * @param password The user's password for verification.
     */
    public static void verifyRightToReadUser(String email, String password) {

    }

}

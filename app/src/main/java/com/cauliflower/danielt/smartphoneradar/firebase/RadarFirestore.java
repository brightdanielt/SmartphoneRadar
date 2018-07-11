package com.cauliflower.danielt.smartphoneradar.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RadarFirestore {
    private static final String TAG = RadarFirestore.class.getSimpleName();

    private static final String FIRESTORE_COLLECTION_USER = "user";
    private static final String FIRESTORE_COLLECTION_COORDINATE = "coordinate";

    private static final String FIRESTORE_FIELD_UID = "uid";
    private static final String FIRESTORE_FIELD_ORIGINAL_PASSWORD = "originalPassword";
    private static final String FIRESTORE_FIELD_PASSWORD = "password";
    private static final String FIRESTORE_FIELD_MODEL = "model";
    private static final String FIRESTORE_FIELD_IMEI = "imei";
    private static final String FIRESTORE_FIELD_LATITUDE = "latitude";
    private static final String FIRESTORE_FIELD_LONGITUDE = "longitude";
    private static final String FIRESTORE_FIELD_TIME = "time";

    /**
     * In firestore, an user is composed by email, password, imei, uid, model.
     * <p>
     * After sign in facebook ,we create the user to firestore.
     * Beside, firestore security rule will verify if {@link FirebaseUser#getUid()} exists,
     * if the user didn't sign in, this create would failed.
     *
     * @param email    Email get from {@link FirebaseUser#getEmail()}.
     * @param password User's customized password.
     */
    public static void createUser(String email, String password, String imei, String model, String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put(FIRESTORE_FIELD_PASSWORD, password);
        user.put(FIRESTORE_FIELD_IMEI, imei);
        user.put(FIRESTORE_FIELD_MODEL, model);
        user.put(FIRESTORE_FIELD_UID, uid);
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully create.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error create document", e);
                    }
                });
    }

    /**
     * Before update password ,firestore security rule will verify
     * if the original password is equal to password in firebase,
     * if the verification passed ,the user can update the password.
     * <p>
     * Unfortunately!!!!! The firestore is beta version, we can not update only one field,
     * we must update every fields in the document.
     *
     * @param email            Email get from {@link FirebaseUser#getEmail()}.
     * @param originalPassword The original password for verification.
     * @param newPassword      The new customized password.
     */
    public static void updatePassword(String email, String originalPassword, String newPassword,
                                      String imei, String model, String uid) {
        Map<String, Object> user = new HashMap<>();
        user.put(FIRESTORE_FIELD_ORIGINAL_PASSWORD, originalPassword);
        user.put(FIRESTORE_FIELD_PASSWORD, newPassword);
        user.put(FIRESTORE_FIELD_IMEI, imei);
        user.put(FIRESTORE_FIELD_MODEL, model);
        user.put(FIRESTORE_FIELD_UID, uid);
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
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


}

package com.cauliflower.danielt.smartphoneradar.firebase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.cauliflower.danielt.smartphoneradar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadarFirestore {
    private static final String TAG = RadarFirestore.class.getSimpleName();

    private static final String FIRESTORE_COLLECTION_USER = "user";
    private static final String FIRESTORE_COLLECTION_COORDINATE = "coordinate";

    private static final String FIRESTORE_FIELD_EMAIL = "email";
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
        user.put(FIRESTORE_FIELD_EMAIL, email);
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
        user.put(FIRESTORE_FIELD_EMAIL, email);
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
     * A user can track location of multiple users so the user could has a user list to
     * choose which user to track, before adding a user to the list, you should prove that
     * you have the right to read the user.
     * <p>
     * If we can get {@link QueryDocumentSnapshot} in callback,
     * it means the user has right to read this user.
     *
     * @param email    The email that user sign in facebook.
     * @param password The user's password for verification.
     */
    public static void checkRightToReadUser(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .whereEqualTo(FIRESTORE_FIELD_EMAIL, email)
                .whereEqualTo(FIRESTORE_FIELD_PASSWORD, password)
                //The only security rule is limit == 1
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error listing documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * Before create location ,firestore security rule will verify if the uid
     * from {@link FirebaseUser#getUid()} exists,
     * if the user didn't sign in, there would be no uid and this create would failed.
     *
     * @param email     Email get from {@link FirebaseUser#getEmail()}.
     * @param imei      The imei for verification.
     * @param latitude  The latitude of the device.
     * @param longitude The longitude of the device.
     */
    public static void createLocation(String coordinateId, String password, String email, String uid,
                                      String imei, double latitude, double longitude) {
        Map<String, Object> coordinate = new HashMap<>();
        coordinate.put(FIRESTORE_FIELD_TIME, new Date());
        coordinate.put(FIRESTORE_FIELD_LATITUDE, latitude);
        coordinate.put(FIRESTORE_FIELD_LONGITUDE, longitude);
        coordinate.put(FIRESTORE_FIELD_IMEI, imei);
        coordinate.put(FIRESTORE_FIELD_PASSWORD, password);
        coordinate.put(FIRESTORE_FIELD_UID, uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document(coordinateId)
                .set(coordinate)
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
     * Before update location ,firestore security rule will verify
     * if the imei, uid, password of the user is equal to correspond values in firebase,
     * if the verification passed ,the user can update the location.
     *
     * @param email     Email get from {@link FirebaseUser#getEmail()}.
     * @param password  The password of user for verification.
     * @param uid       The uid of user for verification.
     * @param imei      The imei of device for verification.
     * @param latitude  The latitude of the device.
     * @param longitude The longitude of the device.
     */
    public static void updateLocation(String coordinateId, String email, String password, String uid,
                                      String imei, double latitude, double longitude) {
        Map<String, Object> coordinate = new HashMap<>();
        coordinate.put(FIRESTORE_FIELD_TIME, new Date());
        coordinate.put(FIRESTORE_FIELD_LATITUDE, latitude);
        coordinate.put(FIRESTORE_FIELD_LONGITUDE, longitude);
        coordinate.put(FIRESTORE_FIELD_IMEI, imei);
        coordinate.put(FIRESTORE_FIELD_PASSWORD, password);
        coordinate.put(FIRESTORE_FIELD_UID, uid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document(coordinateId)
                .set(coordinate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully update.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error update document", e);
                    }
                });
    }

    /**
     * The user don't have to sign in facebook to query new location.
     * <p>
     * Before setting listener to location ,firestore security rule will verify
     * if the password is equal to password in firebase,
     * if the verification passed ,the user can set listener to the location.
     * <p>
     * If there is any update in the collection_coordinate,
     * the {@link EventListener#onEvent(Object, FirebaseFirestoreException)} will trigger.
     *
     * @param email    The email that user sign in facebook.
     * @param password The user's password for verification.
     */
    public static void setOnLocationUpdateListener(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                //Listen to multiple documents in  collection_coordinate
                .whereEqualTo(FIRESTORE_FIELD_PASSWORD, password)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get(FIRESTORE_FIELD_PASSWORD) != null) {
                                Log.d(TAG, "onCoordinateUpdate," + "\n" +
                                        "time: " + doc.getDate(FIRESTORE_FIELD_TIME) + "\n" +
                                        "lat: " + doc.getDouble(FIRESTORE_FIELD_LATITUDE) + "\n" +
                                        "lng: " + doc.getDouble(FIRESTORE_FIELD_LONGITUDE) + "\n");
                            }
                        }
                    }
                });
    }


}

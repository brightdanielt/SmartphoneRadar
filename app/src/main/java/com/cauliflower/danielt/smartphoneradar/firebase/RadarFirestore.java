package com.cauliflower.danielt.smartphoneradar.firebase;

import com.cauliflower.danielt.smartphoneradar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RadarFirestore {
    private static final String TAG = RadarFirestore.class.getSimpleName();

    /**
     * <p> In user collection, there are one or more than one document:
     * <p>
     * <p> | collection name |    | document id |
     * <p>        user         -  daniel@gmail.com
     * <p>                     -  danny@gmail.com
     * <p>                     -  david@gmail.com
     * -------------------------------------------------------------------------------------------
     * <p> In document of user collection, there are one collection and five fields:
     * <p>
     * <p> daniel@gmail.com - Collection coordinate
     * <p>                  - field uid : 132143543654765757
     * <p>                  - field email : daniel@gmail.com
     * <p>                  - field password : n8nruobjeh4bvm
     * <p>                  - field model : HTC_one
     * <p>                  - field imei : 123456789
     * -------------------------------------------------------------------------------------------
     * <p> In coordinate collection, there is only one document now:
     * <p>
     * <p> | collection name |    | document id |
     * <p>      coordinate     -        1
     * -------------------------------------------------------------------------------------------
     * <p> In document of coordinate collection, there are six fields:
     * <p>
     * <p>  1 - field uid : 132bgj476hj5757yio
     * <p>    - field password : n8nruobjeh4bvm
     * <p>    - field imei : 123456789
     * <p>    - field latitude : 123.321
     * <p>    - field longitude : 321.123
     * <p>    - field time : 2018-07-08 21:10:10
     */

    public static final String FIRESTORE_COLLECTION_USER = "user";
    public static final String FIRESTORE_COLLECTION_COORDINATE = "coordinate";

    public static final String FIRESTORE_FIELD_EMAIL = "email";
    public static final String FIRESTORE_FIELD_UID = "uid";
    public static final String FIRESTORE_FIELD_ORIGINAL_PASSWORD = "originalPassword";
    public static final String FIRESTORE_FIELD_PASSWORD = "password";
    public static final String FIRESTORE_FIELD_MODEL = "model";
    public static final String FIRESTORE_FIELD_IMEI = "imei";
    public static final String FIRESTORE_FIELD_LATITUDE = "latitude";
    public static final String FIRESTORE_FIELD_LONGITUDE = "longitude";
    public static final String FIRESTORE_FIELD_TIME = "time";

    /**
     * After sign in facebook ,we create the user to firestore.
     * Beside, firestore security rule will verify if password, imei, uid, model are not null,
     * if the user didn't sign in facebook, this create would failed.
     *
     * @param email    Email get from {@link FirebaseUser#getEmail()}.
     * @param uid      Email get from {@link FirebaseUser#getUid()}.
     * @param password RadarUser's customized password.
     * @param model    The model of the android device.
     * @param imei     The imei of the android device.
     */
    public static void createUser(String email, String password, String imei, String model, String uid,
                                  OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
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
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
                /*.addOnSuccessListener(new OnSuccessListener<Void>() {
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
                });*/
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
    public static void updatePassword(final String email, String originalPassword, final String newPassword) {
        Map<String, Object> user = new HashMap<>();
        user.put(FIRESTORE_FIELD_EMAIL, email);
        user.put(FIRESTORE_FIELD_ORIGINAL_PASSWORD, originalPassword);
        user.put(FIRESTORE_FIELD_PASSWORD, newPassword);
        // Access a Cloud Firestore instance
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //更新成功時，同時更新 coordinate 中的密碼
                        Map<String, Object> location = new HashMap<>();
                        location.put(FIRESTORE_FIELD_PASSWORD, newPassword);
                        db.collection(FIRESTORE_COLLECTION_USER)
                                .document(email)
                                .collection(FIRESTORE_COLLECTION_COORDINATE)
                                .document("1")
                                .set(location, SetOptions.merge());
                    }
                });
                /*.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });*/

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
    public static void checkRightToReadUser(String email, String password, OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .whereEqualTo(FIRESTORE_FIELD_EMAIL, email)
                .whereEqualTo(FIRESTORE_FIELD_PASSWORD, password)
                //The only security rule is limit == 1
                .limit(1)
                .get()
                .addOnCompleteListener(listener);
                /*.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                });*/
    }

    /**
     * Check if the user exists in firestore.
     *
     * @param email The email that user sign in facebook.
     */
    public static void checkUserExists(String email, OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .whereEqualTo(FIRESTORE_FIELD_EMAIL, email)
                //The only security rule is limit == 1
                .limit(1)
                .get()
                .addOnCompleteListener(listener);
                /*.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                });*/
    }

    /**
     * Before create location, firestore security rule will verify if the uid
     * from {@link FirebaseUser#getUid()} exists,
     * if the user didn't sign in, there would be no uid and this create would failed.
     *
     * @param email     From {@link FirebaseUser#getEmail()}.
     * @param password  User's custom password.
     * @param uid       From {@link FirebaseUser#getUid()}.
     * @param imei      The imei of the android device
     * @param latitude  The latitude of the android device.
     * @param longitude The longitude of the android device.
     */
    public static void createLocation(String docId, String email, String password, String uid,
                                      String imei, double latitude, double longitude,
                                      OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Map<String, Object> coordinate = new HashMap<>();
        coordinate.put(FIRESTORE_FIELD_TIME, new Date());
        coordinate.put(FIRESTORE_FIELD_PASSWORD, password);
        coordinate.put(FIRESTORE_FIELD_LATITUDE, latitude);
        coordinate.put(FIRESTORE_FIELD_LONGITUDE, longitude);
        coordinate.put(FIRESTORE_FIELD_IMEI, imei);
        coordinate.put(FIRESTORE_FIELD_UID, uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document(docId)
                .set(coordinate)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
                /*.addOnSuccessListener(new OnSuccessListener<Void>() {
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
                });*/
    }

    /**
     * Before update location ,firestore security rule will verify
     * if the imei is equal to correspond values in firebase,
     * if the verification passed ,the user can update the location.
     *
     * ˇ#$%^&* I am sorry ,there is a method {@link SetOptions#merge()} , we can use it as the
     * second param in {@link com.google.firebase.firestore.DocumentReference#set(Object, SetOptions)}
     * to avoid overwriting entire documents.
     *
     * @param email     From {@link FirebaseUser#getEmail()}.
     * @param imei      The imei of the android device.
     * @param latitude  The latitude of the android device.
     * @param longitude The longitude of the android device.
     */
    public static void updateLocation(String coordinateId, String email, String imei,
                                      double latitude, double longitude,
                                      OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Map<String, Object> coordinate = new HashMap<>();
        coordinate.put(FIRESTORE_FIELD_IMEI, imei);
        coordinate.put(FIRESTORE_FIELD_TIME, new Date());
        coordinate.put(FIRESTORE_FIELD_LATITUDE, latitude);
        coordinate.put(FIRESTORE_FIELD_LONGITUDE, longitude);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document(coordinateId)
                .set(coordinate, SetOptions.merge())
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
                /*.addOnSuccessListener(new OnSuccessListener<Void>() {
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
                });*/
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
     * @return ListenerRegistration ,we can stop listening by calling ListenerRegistration.remove();
     */
    public static ListenerRegistration setOnLocationUpdateListener(String email, String password,
                                                                   EventListener<QuerySnapshot> onLocationUpdate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(FIRESTORE_COLLECTION_USER)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                //Listen to multiple documents in  collection_coordinate
                .whereEqualTo(FIRESTORE_FIELD_PASSWORD, password);
        return query.addSnapshotListener(onLocationUpdate);
                /*.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                });*/
    }


}

package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = FirebaseActivity.class.getSimpleName();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        initView();
        updateAuthInfo(currentUser);
    }

    private void initView() {
        Button btn_Sign = findViewById(R.id.btn_sign);
        btn_Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    signOut();
                } else {
                    signIn();
                }
            }
        });
        Button btn_getFirestoreData = findViewById(R.id.btn_getFirestoreData);
        btn_getFirestoreData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                writeFirestoreLocationDocument(mAuth.getCurrentUser().getEmail());
                listenFirebaseLocation(mAuth.getCurrentUser().getEmail(), "123456789");
//                getFirestoreLocationDocument(mAuth.getCurrentUser().getEmail());
//                signUp(mAuth.getCurrentUser().getEmail(), "", "");
//                getFirestorePhoneInfoDoc(mAuth.getCurrentUser().getEmail());
            }
        });

    }

    /**
     * After sign in, sign out, onStart,
     * we should update ui to show if the user has sign in or sign out
     */
    private void updateAuthInfo(FirebaseUser currentUser) {
        TextView tv_userInfo = findViewById(R.id.tv_fbInfo);
        Button btn_Sign = findViewById(R.id.btn_sign);
        ImageView fab_photo = findViewById(R.id.fab_photo);
        if (currentUser != null) {
            tv_userInfo.setText("FacebookInfo:" + "\n" +
                    "UID: " + currentUser.getUid() + "\n" +
                    "DisplayName: " + currentUser.getDisplayName() + "\n" +
                    "E-mail: " + currentUser.getEmail() + "\n" +
                    "phone: " + currentUser.getPhoneNumber() + "\n" +
                    "ProviderID: " + currentUser.getProviderId() + "\n"
            );
            new LoadBitmapFromUri().execute(currentUser.getPhotoUrl());
            btn_Sign.setText("Sign out");
        } else {
            tv_userInfo.setText("未登入Firebase Auth");
            fab_photo.setImageBitmap(null);
            btn_Sign.setText("Sign In");
        }
    }

    /**
     * Sign in with firestore authentication
     */
    public void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
//                new AuthUI.IdpConfig.TwitterBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * Sign out with firestore authentication
     */
    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        updateAuthInfo(mAuth.getCurrentUser());
                    }
                });

    }

    /**
     * Delete user with firestore authentication
     */
    private void deleteUser() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Get result from firebase sign in
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.i(TAG, "Name: " + user.getDisplayName() + "\n" +
                        "UID: " + user.getUid());

                updateAuthInfo(mAuth.getCurrentUser());
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                int errorCode = response.getError().getErrorCode();
                String errorMsg = response.getError().getMessage();
                Log.i(TAG, "ErrorCode: " + errorCode + "Msg: " + errorMsg);
            }
        }
    }

    private class LoadBitmapFromUri extends AsyncTask<Uri, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Uri... uris) {
            Log.i(TAG, uris[0].toString());
            try {
                URL url = new URL(uris[0].toString());
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                ImageView photo = findViewById(R.id.fab_photo);
                photo.setImageBitmap(bitmap);
            }
            super.onPostExecute(bitmap);
        }
    }

    private static final String FIRESTORE_COLLECTION_LOCATION = "location";
    private static final String FIRESTORE_COLLECTION_COORDINATE = "coordinate";
    private static final String FIRESTORE_FIELD_LATITUDE = "latitude";
    private static final String FIRESTORE_FIELD_LONGITUDE = "longitude";
    private static final String FIRESTORE_FIELD_TIME = "time";
    private static final String FIRESTORE_COLLECTION_PHONE_INFO = "phoneInfo";
    private static final String FIRESTORE_FIELD_MODEL = "model";
    private static final String FIRESTORE_FIELD_IMEI = "imei";
    private static final String FIRESTORE_AUTH_UID = "uid";


    private void getFirestoreLocationDocument(String email) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("location")
                .document(email)
                .collection("coordinate")
                .document("01");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        TextView tv_firestoreData = findViewById(R.id.tv_firestoreData);
                        tv_firestoreData.setText(document.getData() + "");
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void listenFirebaseLocation(String email, String imei) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_LOCATION)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document("01")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d(TAG, "Current data: " + snapshot.getData());
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    }
                });
                /*.whereEqualTo(FIRESTORE_FIELD_IMEI, imei)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get(FIRESTORE_FIELD_TIME) != null) {
                                Log.d(TAG, "data: " + doc.getData());
                            }
                        }
                        Log.d(TAG,"GGGG");
                    }
                });*/

    }

    private void writeFirestoreLocationDocument(String email) {
        final boolean success[] = {false};
        Map<String, Object> location = new HashMap<>();
        location.put(FIRESTORE_FIELD_LATITUDE, 222.0);
        location.put(FIRESTORE_FIELD_LONGITUDE, 333.0);
        location.put(FIRESTORE_FIELD_TIME, new Date());
        location.put(FIRESTORE_FIELD_IMEI, "123456789");
        location.put(FIRESTORE_AUTH_UID, mAuth.getCurrentUser().getUid());
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_LOCATION)
                .document(email)
                .collection(FIRESTORE_COLLECTION_COORDINATE)
                .document("01")
                .set(location)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written.");
                        success[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        success[0] = false;
                    }
                });
    }


    //註冊
    public boolean signUp(String email, String model, String IMEI) {
        final boolean[] success = {false};
        Map<String, Object> phoneInfo = new HashMap<>();
        phoneInfo.put(FIRESTORE_FIELD_MODEL, "HTC_m11");
        phoneInfo.put(FIRESTORE_FIELD_IMEI, "123456789");
        phoneInfo.put(FIRESTORE_AUTH_UID, mAuth.getCurrentUser().getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_PHONE_INFO).document(email)
                .set(phoneInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written.");
                        success[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        success[0] = false;
                    }
                });
        return success[0];
    }

    public boolean getFirestorePhoneInfoDoc(String email) {
        final boolean[] success = {false};
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FIRESTORE_COLLECTION_PHONE_INFO).document(email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        Log.d(TAG, "DocumentSnapshot successfully read.");
                        TextView tv_firestoreData = findViewById(R.id.tv_firestoreData);
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        tv_firestoreData.setText(document.getData() + "");
                        success[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error read document", e);
                        success[0] = false;
                    }
                });
        return success[0];
    }

}

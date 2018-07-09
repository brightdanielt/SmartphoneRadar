package com.cauliflower.danielt.smartphoneradar.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cauliflower.danielt.smartphoneradar.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
                Log.i(TAG, "Name: " + user.getDisplayName());

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

}

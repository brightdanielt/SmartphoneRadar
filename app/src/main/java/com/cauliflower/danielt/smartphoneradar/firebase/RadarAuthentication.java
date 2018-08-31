package com.cauliflower.danielt.smartphoneradar.firebase;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * We use firebase authentication to verify the user is a real human,
 * after the user is verified successfully in other word the user sign in facebook successfully,
 * we should create the user to firestore.
 */
public class RadarAuthentication {

    /**
     * Sign in with firebase authentication
     */
    public static void signIn(Context context, int requestCode) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.PhoneBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().build(),
//                new AuthUI.IdpConfig.TwitterBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        // Create and launch sign-in intent
        ((Activity) context).startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                requestCode);
    }

    /**
     * Sign out with firebase authentication
     */
    public static void signOut(Context context, @Nullable OnCompleteListener<Void> listener) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(listener);
    }

    /**
     * Delete user with firebase authentication
     */
    public static void deleteUser(Context context, OnCompleteListener<Void> listener) {
        AuthUI.getInstance()
                .delete(context)
                .addOnCompleteListener(listener);
    }

    public static Uri getDifferentPhotoSize(FirebaseUser user, int height) {
        if (user != null && user.getPhotoUrl() != null) {

            return user.getPhotoUrl().buildUpon().appendQueryParameter("type", "large").build();
//            return photoUri.buildUpon().appendQueryParameter("height",String.valueOf(height)).build();
        }
        return null;
    }
}

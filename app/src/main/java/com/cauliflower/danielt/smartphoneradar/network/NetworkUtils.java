package com.cauliflower.danielt.smartphoneradar.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkUtils {
    private NetworkUtils() {
    }

    /**
     * @param context use to access {@link ConnectivityManager}
     * @return {@code true} if the {@link NetworkInfo.State} equals to {@link NetworkInfo.State#CONNECTED},
     * {@code false} if no default network is currently active or there is no network connectivity exists
     */
    public static boolean checkNetworkConnected(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.d(context.getClass().getSimpleName(), "Can not get system service : CONNECTIVITY_SERVICE");
            return false;
        }
        try {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }
}

package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
        try {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null) {
                return info.isConnected();
            }
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }
}

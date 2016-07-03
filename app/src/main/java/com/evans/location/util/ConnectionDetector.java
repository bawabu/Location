package com.evans.location.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by evans on 10/21/15.
 */
public class ConnectionDetector {

    private Context mContext;

    public ConnectionDetector(Context context) {
        this.mContext = context;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

}

package com.evans.location.service;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.evans.location.app.AppController;
import com.evans.location.util.ConnectionDetector;
import com.evans.location.util.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Get location updates and update a user's location in the database.
 *
 * Created by evans on 10/20/15.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = LocationHelper.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 300000;
    private static final int FASTEST_INTERVAL = 60000;
    private static final int DISPLACEMENT = 10;

    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LAST_UPDATED = "last_updated";

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private CallStateListener mCallStateListener;

    public LocationHelper(Context context) {
        this.mContext = context;
        mCallStateListener = new CallStateListener();
    }

    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    updateLocation();
                    break;
                default:
                    super.onCallStateChanged(state, incomingNumber);
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(mContext.getApplicationContext(),
                        GooglePlayServicesUtil.getErrorString(resultCode), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "This device is not supported.", Toast.LENGTH_LONG).show();
            }

            return false;
        }

        return true;
    }

    private void updateLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            SessionManager sessionManager = new SessionManager(mContext);
            ConnectionDetector connectionDetector = new ConnectionDetector(mContext);

            if (sessionManager.isLoggedIn() && connectionDetector.isConnectedToInternet()) {
                String phoneNumber = sessionManager.getUserDetails().get(SessionManager.KEY_PHONE);
                final Date date = new Date();
                String url = "http://locatecall.netne.net/location_update.php";
                final String number;

                if (phoneNumber.substring(0, 1).equals("0"))
                    number = phoneNumber.substring(1).replaceAll(" ", "");
                else if (phoneNumber.substring(0, 4).equals("+254"))
                    number = phoneNumber.substring(4).replaceAll(" ", "");
                else
                    number = phoneNumber;

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put(KEY_PHONE_NUMBER, number);
                        params.put(KEY_LATITUDE, String.valueOf(latitude));
                        params.put(KEY_LONGITUDE, String.valueOf(longitude));
                        params.put(KEY_LAST_UPDATED, dateFormat.format(date.getTime()));

                        return params;
                    }
                };

                AppController.getInstance().addToRequestQueue(stringRequest);
                Log.d(TAG, "Inserted");
            }
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void start() {
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            if (mGoogleApiClient.isConnected())
                startLocationUpdates();
        }

        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void stop() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
                stopLocationUpdates();
            }
        }

        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
    }
}

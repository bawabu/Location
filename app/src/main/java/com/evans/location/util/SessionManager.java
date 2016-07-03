package com.evans.location.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.evans.location.LoginActivity;

import java.util.HashMap;

/**
 * Created by evans on 10/18/15.
 */
public class SessionManager {

    private SharedPreferences mPreferences;

    private SharedPreferences.Editor mEditor;

    private Context mContext;

    private static final int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "Location";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_PHONE = "phone";

    public SessionManager(Context mContext) {
        this.mContext = mContext;
        mPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPreferences.edit();
    }

    public void createLoginSession(String phoneNumber) {
        mEditor.putBoolean(IS_LOGIN, true);
        mEditor.putString(KEY_PHONE, phoneNumber);

        mEditor.commit();
    }

    /**
     * redirect user if not logged in
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent intent = new Intent(mContext, LoginActivity.class);
//            close all activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            flag to start new activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(intent);
        }
    }

    /**
     * Get stored session data
     * @return user
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();

        user.put(KEY_PHONE, mPreferences.getString(KEY_PHONE, null));

        return user;
    }

    /**
     * Logout a user
     */
    public void logoutUser() {
//        clear all data from shared preferences
        mEditor.clear();
        mEditor.commit();

        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(intent);
    }

    public boolean isLoggedIn() {
        return mPreferences.getBoolean(IS_LOGIN, false);
    }
}

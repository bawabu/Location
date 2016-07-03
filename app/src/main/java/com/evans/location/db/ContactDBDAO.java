package com.evans.location.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by evans on 10/19/15.
 */
public class ContactDBDAO {

    protected SQLiteDatabase database;
    private DatabaseHelper mDatabaseHelper;
    private Context mContext;

    public ContactDBDAO(Context context) {
        this.mContext = context;
        mDatabaseHelper = DatabaseHelper.getHelper(context);
        try {
            open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        if (mDatabaseHelper == null)
            mDatabaseHelper = DatabaseHelper.getHelper(mContext);

        database = mDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDatabaseHelper.close();
        database = null;
    }
}

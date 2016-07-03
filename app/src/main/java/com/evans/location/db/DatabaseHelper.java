package com.evans.location.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by evans on 10/19/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts_db";
    private static final int DATABASE_VERSION = 2;

    public static final String CONTACTS_TABLE = "contacts";

    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String PHONE_COLUMN = "phone";

    public static final String CREATE_CONTACTS_TABLE = "CREATE TABLE " + CONTACTS_TABLE + "("
            + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME_COLUMN + " TEXT NOT NULL, "
            + PHONE_COLUMN + " TEXT NOT NULL" + ")";

    private static DatabaseHelper mDatabaseHelper;

    public static synchronized DatabaseHelper getHelper(Context context) {
        if (mDatabaseHelper == null)
            mDatabaseHelper = new DatabaseHelper(context);

        return mDatabaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE);
        onCreate(db);
    }
}

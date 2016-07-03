package com.evans.location.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.evans.location.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evans on 10/19/15.
 */
public class ContactDAO extends ContactDBDAO {

    private static final String TAG = ContactDAO.class.getSimpleName();

    private String[] mColumns = {
            DatabaseHelper.ID_COLUMN,
            DatabaseHelper.NAME_COLUMN,
            DatabaseHelper.PHONE_COLUMN
    };

    public ContactDAO(Context context) {
        super(context);
    }

    public void insertContact(Contact contact) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.NAME_COLUMN, contact.getContactName());
        values.put(DatabaseHelper.PHONE_COLUMN, contact.getContactPhoneNumber());

        database.insert(DatabaseHelper.CONTACTS_TABLE, null, values);

        Log.d(TAG, "Inserted");
        database.close();
    }

    public void deleteContact(Contact contact) {
        int id = contact.getId();
        database.delete(DatabaseHelper.CONTACTS_TABLE, DatabaseHelper.ID_COLUMN + " = " + id, null);
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.CONTACTS_TABLE, mColumns, null, null, null,
                null, DatabaseHelper.ID_COLUMN + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact = new Contact();
            contact.setId(Integer.parseInt(cursor.getString(0)));
            contact.setContactName(cursor.getString(1));
            contact.setContactPhoneNumber(cursor.getString(2));

            Log.d(TAG, cursor.getString(2));

            contacts.add(contact);

            cursor.moveToNext();
        }

        cursor.close();

        return contacts;
    }
}

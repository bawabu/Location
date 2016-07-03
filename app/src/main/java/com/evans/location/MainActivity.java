package com.evans.location;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.evans.location.adapter.ContactListAdapter;
import com.evans.location.db.ContactDAO;
import com.evans.location.model.Contact;
import com.evans.location.service.LocationUpdateService;
import com.evans.location.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PICK_CONTACTS = 1;

    private List<Contact> mContacts;
    private RecyclerView mRecyclerView;
    private ContactListAdapter mAdapter;

    SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSessionManager = new SessionManager(getApplicationContext());
        mSessionManager.checkLogin();
        HashMap<String, String> user = mSessionManager.getUserDetails();
        String phone_number = user.get(SessionManager.KEY_PHONE);

        Snackbar.make(findViewById(R.id.contactsList),
                "Phone Number: " + phone_number, Snackbar.LENGTH_SHORT).show();

        Intent locationIntent = new Intent(this, LocationUpdateService.class);
        startService(locationIntent);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addContact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, PICK_CONTACTS);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.contactsRecyclerView);

        registerForContextMenu(mRecyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                final int itemPosition = parent.getChildAdapterPosition(view);
                if (itemPosition == mRecyclerView.NO_POSITION)
                    return;

                final int itemCount = state.getItemCount();
                Log.d(TAG, "Item count: " + itemCount);

                if (itemPosition == 0)
                    outRect.set(0, 0, 0, 0);
                else if (itemCount > 0 && itemPosition == itemCount - 1)
                    outRect.set(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.last_recycler_item));
                else
                    outRect.set(0, 0, 0, 0);

            }
        });

        LoadContacts loadContacts = new LoadContacts();
        loadContacts.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());

            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            Contact contact = new Contact(getContactPhoneNumber(cursor),
                    getContactName(cursor));

            ContactDAO contactDAO = new ContactDAO(getApplicationContext());
            contactDAO.insertContact(contact);

            cursor.close();

            mAdapter.add(0, contact);
            mRecyclerView.scrollToPosition(0);

//            finish();
//            startActivity(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                mSessionManager.logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private String getContactName(Cursor cursor) {
        String contactName = null;

        if (cursor.moveToFirst())
            contactName = cursor.getString
                    (cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        Log.d(TAG, "Contact name: " + contactName);

        return contactName;
    }

    private String getContactPhoneNumber(Cursor cursor) {
        String contactNumber = null;

        if (cursor.moveToFirst())
            contactNumber = cursor.getString
                    (cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

        Log.d(TAG, "Contact number: " + contactNumber);

        return contactNumber;
    }

    private class LoadContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mContacts = new ArrayList<>();

            ContactDAO contactDAO = new ContactDAO(getApplicationContext());
            mContacts = contactDAO.getAllContacts();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mAdapter =
                    new ContactListAdapter(getApplicationContext(), MainActivity.this, mContacts);
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}

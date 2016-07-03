package com.evans.location;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.evans.location.app.AppController;
import com.evans.location.util.ConnectionDetector;
import com.evans.location.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private SessionManager mSessionManager;
    private ConnectionDetector mConnectionDetector;


    private EditText mEditTextPhone;
    private TextInputLayout mInputLayoutPhone;
    private Button mButtonEnter;

    private ProgressDialog mProgressDialog;

    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_JOINED = "joined_on";

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mConnectionDetector = new ConnectionDetector(this);

        mSessionManager = new SessionManager(getApplicationContext());

        mEditTextPhone = (EditText) findViewById(R.id.input_phoneNumber);
        mEditTextPhone.addTextChangedListener(new MyTextWatcher(mEditTextPhone));

        mInputLayoutPhone = (TextInputLayout) findViewById(R.id.inputLayout_phoneNumber);

        mButtonEnter = (Button) findViewById(R.id.btn_login);

        mButtonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateNumber()) {
                    String phoneNumber = mEditTextPhone.getText().toString().trim();

                    if (mConnectionDetector.isConnectedToInternet()) {
                        mSessionManager.createLoginSession(phoneNumber);

                        mProgressDialog = new ProgressDialog(LoginActivity.this);
                        mProgressDialog.setMessage("Loading...");
                        mProgressDialog.show();

                        registerUser(phoneNumber);

                        hideProgressDialog();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.loginLayout),
                                getString(R.string.no_internet), Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        TextView textView = (TextView)
                                sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat
                                .getColor(getApplicationContext(), R.color.warning));
                        snackbar.show();
                    }
                }
            }
        });
    }

    private void registerUser(String phoneNumber) {
        final String number;
        final Date date = new Date();
        String url = "http://locatecall.netne.net/register.php";

        if (phoneNumber.substring(0, 1).equals("0"))
            number = phoneNumber.substring(1).replaceAll(" ", "");
        else if (phoneNumber.substring(0, 4).equals("+254"))
            number = phoneNumber.substring(4).replaceAll(" ", "");
        else
            number = "123456789";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        hideProgressDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        hideProgressDialog();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(KEY_PHONE_NUMBER, number);
                params.put(KEY_JOINED, dateFormat.format(date.getTime()));

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        public MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.input_phoneNumber:
                    validateNumber();
                    break;
            }
        }
    }

    private boolean validateNumber() {
        String phone_number = mEditTextPhone.getText().toString().trim();
        if (phone_number.isEmpty()) {
            mInputLayoutPhone.setError(getString(R.string.error_empty_phone_number));
            requestFocus(mEditTextPhone);
            return false;
        } else
            mInputLayoutPhone.setErrorEnabled(false);

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus())
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}

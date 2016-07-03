package com.evans.location.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.evans.location.LocationActivity;
import com.evans.location.R;
import com.evans.location.app.AppController;
import com.evans.location.model.Contact;
import com.evans.location.util.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by evans on 10/18/15.
 */
public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private Context mContext;
    private Activity mActivity;
    private List<Contact> mContacts;

    private ProgressDialog mProgressDialog;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView contactImage;
        public TextView contactName;
        public ImageView contactCall;

        public ViewHolder(View itemView) {
            super(itemView);

            contactImage = (ImageView) itemView.findViewById(R.id.contactImage);
            contactName = (TextView) itemView.findViewById(R.id.contactName);
            contactCall = (ImageView) itemView.findViewById(R.id.contactCall);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ConnectionDetector connectionDetector = new ConnectionDetector(mContext);
            final View view = v;
            if (connectionDetector.isConnectedToInternet()) {
                mProgressDialog = new ProgressDialog(mActivity);

                final Contact contact = mContacts.get(getLayoutPosition());
                String phoneNumber = contact.getContactPhoneNumber();
                String number;

                if (phoneNumber.substring(0, 1).equals("0"))
                    number = phoneNumber.substring(1).replaceAll(" ", "");
                else if (phoneNumber.substring(0, 4).equals("+254"))
                    number = phoneNumber.substring(4).replaceAll(" ", "");
                else
                    number = phoneNumber;

                mProgressDialog.setMessage("Fetching coordinates...");
                mProgressDialog.show();

                String url = "http://locatecall.netne.net/get_location.php?phone_number=" + number;

                Log.d("ContactListAdapter", url);

                JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    JSONObject object = response.getJSONObject(0);

                                    if (object.has("information")) {

                                        String information = object.getString("information");
                                        hideProgressDialog();

                                        Snackbar snackbar = Snackbar.make(view, information,
                                                Snackbar.LENGTH_LONG);
                                        View sbView = snackbar.getView();
                                        TextView textView = (TextView)
                                                sbView.findViewById(android.support.design.R.id.snackbar_text);
                                        textView.setTextColor(ContextCompat
                                                .getColor(mContext, R.color.information));
                                        snackbar.show();


                                    } else if (object.has("latitude") && object.has("longitude") &&
                                            object.has("last_updated")) {

                                        double latitude = object.getDouble("latitude");
                                        double longitude = object.getDouble("longitude");
                                        String last_updated = object.getString("last_updated");

                                        Intent intent = new Intent(mContext, LocationActivity.class);
                                        intent.putExtra("contact", contact);
                                        intent.putExtra("latitude",latitude);
                                        intent.putExtra("longitude", longitude);
                                        intent.putExtra("last_updated", last_updated);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                        hideProgressDialog();
                                        mContext.startActivity(intent);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    hideProgressDialog();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                hideProgressDialog();
                            }
                        }
                );

                AppController.getInstance().addToRequestQueue(request);
            } else {
                Snackbar snackbar = Snackbar.make(v, mContext.getString(R.string.no_internet),
                        Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView)
                        sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat
                        .getColor(mContext, R.color.warning));
                snackbar.show();
            }
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public ContactListAdapter(Context context, Activity activity, List<Contact> contacts) {
        this.mContext = context;
        this.mActivity = activity;
        this.mContacts = contacts;
    }

    @Override
    public ContactListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactListAdapter.ViewHolder holder, int position) {

        final Contact contact = mContacts.get(position);
        String contact_name = contact.getContactName();

        holder.contactName.setText(contact_name);

        ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
        int color = colorGenerator.getRandomColor();
        TextDrawable.IBuilder builder = TextDrawable.builder().round();
        TextDrawable textDrawable = builder.build(contact_name.substring(0, 1), color);
        holder.contactImage.setImageDrawable(textDrawable);

        holder.contactCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tel = "tel:" + contact.getContactPhoneNumber();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(tel));
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(callIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public void add(int position, Contact contact) {
        mContacts.add(position, contact);
        notifyItemInserted(position);
    }

    public Contact remove(int position) {
        final Contact contact = mContacts.remove(position);
        notifyItemRemoved(position);
        return contact;
    }
}

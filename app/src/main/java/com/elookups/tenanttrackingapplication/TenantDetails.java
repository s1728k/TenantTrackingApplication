package com.elookups.tenanttrackingapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TenantDetails extends AppCompatActivity {

    private int id, tId;
    private int hi = 0, ai = 0;
    private static Date receivedDate;

    private JSONArray rentHouseArray;
    private JSONObject selectedRentHouse = new JSONObject();

    private JSONArray tenantsArray;
    private JSONObject selectedTenant = new JSONObject();

    private JSONArray arrRents;
    private JSONArray arrRentRates;

    public static class ReceivedDate  extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            String temp = day + "/" + (month+1) + "/" + year;
            try {
                receivedDate = new SimpleDateFormat("dd/MM/yyyy").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            EditText newReceivedDate = (EditText) getActivity().findViewById(R.id.newReceivedDate);
            newReceivedDate.setText(temp);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tenant_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        Bundle extraBundle = intentExtras.getExtras();
        id = extraBundle.getInt("id");
        tId = extraBundle.getInt("tId");

        try {
            rentHouseArray = Misc.getRentHouseArray(TenantDetails.this);
            selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);
            tenantsArray = selectedRentHouse.getJSONArray("tenants");
            selectedTenant = Misc.getSelectedObject(tId, tenantsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ImageView houseImages = (ImageView) findViewById(R.id.houseImages);
        houseImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hi = hi + 1;
                    hi = hi % selectedRentHouse.getJSONArray("house_images").length();
                    houseImages.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("house_images").getJSONObject(hi).getString("img_path"), 100, 100));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        final ImageView attachImage = (ImageView) findViewById(R.id.attachImage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ai = ai + 1;
                    ai = ai % selectedTenant.getJSONArray("attach_images").length();
                    attachImage.setImageBitmap(Misc.reducedBitmap(selectedTenant.getJSONArray("attach_images").getJSONObject(ai).getString("img_path"), 100, 100));
                    TextView attachName = (TextView) findViewById(R.id.attachName);
                    attachName.setText(selectedTenant.getJSONArray("attach_images").getJSONObject(ai).getString("attach_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button receiveRentButton = (Button) findViewById(R.id.receiveRentButton);
        receiveRentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject newRent = new JSONObject();
                EditText amountReceived = (EditText) findViewById(R.id.amountReceived);
                try {
                    newRent.put("received_date", receivedDate);
                    newRent.put("amount_paid", Long.parseLong(amountReceived.getText().toString()));
                    arrRents.put(newRent);
                    selectedTenant.put("rent_history", arrRents);
                    selectedTenant.put("previous_dues", Misc.calculatePendingDue(arrRents, selectedTenant.getJSONArray("rent_rate_history")));
                    tenantsArray = Misc.replaceObjectInArray(tId, tenantsArray, selectedTenant);
                    selectedRentHouse.put("tenants", tenantsArray);
                    rentHouseArray = Misc.replaceObjectInArray(id, rentHouseArray, selectedRentHouse);
                    Misc.writeToFile(rentHouseArray.toString(), "dataset.txt", TenantDetails.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Intent intentExtras = new Intent(TenantDetails.this, MainActivity.class);
                intentExtras.putExtra("from", "TenantDetails");
                intentExtras.putExtra("id", id);
                startActivity(intentExtras);
                finish();
            }
        });

        Button editDetailsForTenant = (Button) findViewById(R.id.editDetailsForTenant);
        editDetailsForTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(TenantDetails.this, NewTenant.class);
                intentExtras.putExtra("editMode", "editTenant");
                intentExtras.putExtra("id", id);
                intentExtras.putExtra("tId", tId);
                startActivity(intentExtras);
                finish();
            }
        });

        Button showPaymentHistory = (Button) findViewById(R.id.showPaymentHistory);
        showPaymentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(TenantDetails.this, RentHistory.class);
                intentExtras.putExtra("id", id);
                intentExtras.putExtra("tId", tId);
                intentExtras.putExtra("displayParam", "rent_payment_history");
                startActivity(intentExtras);
            }
        });

        Button showRentRateHistory = (Button) findViewById(R.id.showRentRateHistory);
        showRentRateHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(TenantDetails.this, RentHistory.class);
                intentExtras.putExtra("id", id);
                intentExtras.putExtra("tId", tId);
                intentExtras.putExtra("displayParam", "rent_rate_history");
                startActivity(intentExtras);
            }
        });

        Button showRentHouseDetails = (Button) findViewById(R.id.showRentHouseDetails);
        showRentHouseDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(TenantDetails.this, RentHouseDetails.class);
                intentExtras.putExtra("id", id);
                startActivity(intentExtras);
                finish();
            }
        });

        EditText newReceivedDate = (EditText) findViewById(R.id.newReceivedDate);
        newReceivedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new ReceivedDate();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        try {
            detailSelectedTenant();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void detailSelectedTenant() throws JSONException, ParseException {

        ImageView houseImages = (ImageView) findViewById(R.id.houseImages);
        TextView houseAddress = (TextView) findViewById(R.id.houseAddress);
        houseImages.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("house_images").getJSONObject(0).getString("img_path"), 100, 100));
        houseAddress.setText(selectedRentHouse.getString("house_address"));

        ImageView tenantImage = (ImageView) findViewById(R.id.tenantImage);
        TextView tenantName = (TextView) findViewById(R.id.tenantName);
        tenantImage.setImageBitmap(Misc.reducedBitmap(selectedTenant.getString("img_path"), 100, 100));
        tenantName.setText(selectedTenant.getString("name"));

        ImageView attachImage = (ImageView) findViewById(R.id.attachImage);
        TextView attachName = (TextView) findViewById(R.id.attachName);
        attachImage.setImageBitmap(Misc.reducedBitmap(selectedTenant.getJSONArray("attach_images").getJSONObject(0).getString("img_path"), 100, 100));
        attachName.setText(selectedTenant.getJSONArray("attach_images").getJSONObject(0).getString("attach_name"));

        TextView joinDate = (TextView) findViewById(R.id.joinDate);
        TextView totalRentAgreed = (TextView) findViewById(R.id.totalRentAgreed);
        TextView rentEffectiveFrom = (TextView) findViewById(R.id.rentEffectiveFrom);
        TextView previousDues = (TextView) findViewById(R.id.previousDues);
        TextView advanceReceived = (TextView) findViewById(R.id.advanceReceived);

        joinDate.setText(Misc.anyDateString(Misc.anyDate(selectedTenant.getString("join_date"))));
        totalRentAgreed.setText(Long.toString(selectedTenant.getLong("total_rent_agreed")));
        rentEffectiveFrom.setText(Misc.anyDateString(Misc.anyDate(selectedTenant.getString("rent_effective_from"))));
        previousDues.setText(Long.toString(selectedTenant.getLong("previous_dues")));
        advanceReceived.setText(Long.toString(selectedTenant.getLong("advance_received")));

        arrRents = selectedTenant.getJSONArray("rent_history");

        EditText amountReceived = (EditText) findViewById(R.id.amountReceived);
        EditText newReceivedDate = (EditText) findViewById(R.id.newReceivedDate);
        amountReceived.setText(Long.toString(selectedTenant.getLong("total_rent_agreed")));
        newReceivedDate.setText(Misc.todayString());

        receivedDate = Misc.todayDate();
    }


}

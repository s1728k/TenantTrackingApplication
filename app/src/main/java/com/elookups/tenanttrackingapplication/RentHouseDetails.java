package com.elookups.tenanttrackingapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;

public class RentHouseDetails extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private int id;
    private int hi = 0, ti = 0;
    private int rent_mode;
    private JSONObject selectedRentHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rent_house_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        Bundle extraBundle = intentExtras.getExtras();
        id = extraBundle.getInt("id");

        final ImageView houseImage = (ImageView) findViewById(R.id.houseImage);
        houseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    hi = hi + 1;
                    hi = hi % selectedRentHouse.getJSONArray("house_images").length();
                    houseImage.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("house_images").getJSONObject(hi).getString("img_path"), 100, 100));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        final ImageView tenantImage = (ImageView) findViewById(R.id.tenantImage);
        tenantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ti = ti + 1;
                    ti = ti % selectedRentHouse.getJSONArray("tenants").length();
                    tenantImage.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("tenants").getJSONObject(ti).getString("img_path"), 100, 100));
                    TextView tenantName = (TextView) findViewById(R.id.tenantName);
                    tenantName.setText(selectedRentHouse.getJSONArray("tenants").getJSONObject(ti).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button addNewTenant = (Button) findViewById(R.id.addNewTenant);
        addNewTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(RentHouseDetails.this, NewTenant.class);
                intentExtras.putExtra("editMode", "addNewTenant");
                intentExtras.putExtra("rent_mode", rent_mode);
                intentExtras.putExtra("id", id);
                startActivity(intentExtras);
            }
        });


        Button removeTenantFromThisHouse = (Button) findViewById(R.id.removeTenantFromThisHouse);
        removeTenantFromThisHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray rentHouseArray = null;
                JSONObject selectedRentHouse = new JSONObject();
                JSONArray tenantsArray = null;
                try {
                    rentHouseArray = Misc.getRentHouseArray(RentHouseDetails.this);
                    selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);
                    tenantsArray = selectedRentHouse.getJSONArray("tenants");
                    tenantsArray = Misc.removeObjectAtI(tenantsArray, ti);
                    selectedRentHouse.put("tenants", tenantsArray);
                    rentHouseArray = Misc.replaceObjectInArray(id, rentHouseArray, selectedRentHouse);
                    Misc.writeToFile(rentHouseArray.toString(), "dataset.txt", RentHouseDetails.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intentExtras = new Intent(RentHouseDetails.this, RentHouseDetails.class);
                intentExtras.putExtra("id", id);
                startActivity(intentExtras);
                finish();
            }
        });

        Button editDetailsForThisHouse = (Button) findViewById(R.id.editDetailsForThisHouse);
        editDetailsForThisHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(RentHouseDetails.this, NewRentHouse.class);
                intentExtras.putExtra("editMode", "editRentHouse");
                intentExtras.putExtra("id", id);
                startActivity(intentExtras);
                finish();
            }
        });

        Button showTenantDetails = (Button) findViewById(R.id.showTenantDetails);
        showTenantDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentExtras = new Intent(RentHouseDetails.this, TenantDetails.class);
                intentExtras.putExtra("id", id);
                try {
                    intentExtras.putExtra("tId", selectedRentHouse.getJSONArray("tenants").getJSONObject(ti).getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intentExtras);
                finish();
            }
        });

        Spinner rentModeSpinner = (Spinner) findViewById(R.id.rentModeSpinner);
        rentModeSpinner.setOnItemSelectedListener(this);

        try {
            detailRentHouse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).toString()
        rent_mode = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        rent_mode = 0;
    }

    private void detailRentHouse() throws JSONException {
        JSONArray rentHouseArray = Misc.getRentHouseArray(RentHouseDetails.this);
        selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);
        String rent_mode_text[] = getResources().getStringArray(R.array.rent_mode_spinner);
        String rent_house_size_text[] = getResources().getStringArray(R.array.house_size);

        TextView rentMode = (TextView) findViewById(R.id.rentMode);
        TextView rentMode1 = (TextView) findViewById(R.id.rentMode1);
        TextView rentMode2 = (TextView) findViewById(R.id.rentMode2);
        if(selectedRentHouse.getJSONArray("tenants").length() == 0){
            RelativeLayout tenantRel = (RelativeLayout) findViewById(R.id.tenantRel);
            tenantRel.setVisibility(View.GONE);
            if(selectedRentHouse.getBoolean("allow_sharing")){
                Spinner rentModeSpinner = (Spinner) findViewById(R.id.rentModeSpinner);
                rentModeSpinner.setVisibility(View.VISIBLE);
                rentMode2.setVisibility(View.VISIBLE);
                rentMode.setVisibility(View.GONE);
                rentMode1.setVisibility(View.GONE);
            }else{
                rent_mode = selectedRentHouse.getInt("rent_mode");
                rentMode.setText(rent_mode_text[rent_mode]);
            }
        }else{
            Button addNewTenant = (Button) findViewById(R.id.addNewTenant);
            if(selectedRentHouse.getInt("rent_mode")==1 || selectedRentHouse.getInt("persons_allowed") <= selectedRentHouse.getJSONArray("tenants").length()){
                addNewTenant.setVisibility(View.GONE);
            }

            ImageView tenantImage = (ImageView) findViewById(R.id.tenantImage);
            TextView tenantName = (TextView) findViewById(R.id.tenantName);

            tenantImage.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("tenants").getJSONObject(0).getString("img_path"), 100, 100));
            tenantName.setText(selectedRentHouse.getJSONArray("tenants").getJSONObject(0).getString("name"));
            rent_mode = selectedRentHouse.getInt("rent_mode");
            rentMode.setText(rent_mode_text[rent_mode]);
        }

        ImageView houseImage = (ImageView) findViewById(R.id.houseImage);
        TextView houseAddress = (TextView) findViewById(R.id.houseAddress);
        TextView houseSize = (TextView) findViewById(R.id.houseSize);
        TextView availability = (TextView) findViewById(R.id.availability);
        TextView allowedSize = (TextView) findViewById(R.id.allowedSize);
        TextView soloRentPrice = (TextView) findViewById(R.id.soloRentPrice);
        TextView sharedRentPrice = (TextView) findViewById(R.id.sharedRentPrice);
        TextView advanceAmount = (TextView) findViewById(R.id.advanceAmount);
        TextView advanceShared = (TextView) findViewById(R.id.advanceShared);

        houseImage.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("house_images").getJSONObject(0).getString("img_path"), 100, 100));
        houseAddress.setText(selectedRentHouse.getString("house_address"));
        houseSize.setText(rent_house_size_text[selectedRentHouse.getInt("size")]);
        availability.setText(selectedRentHouse.getString("availability"));

        allowedSize.setText(selectedRentHouse.getString("persons_allowed"));
        soloRentPrice.setText(Long.toString(selectedRentHouse.getLong("solo_rent_price")));
        sharedRentPrice.setText(Integer.toString(selectedRentHouse.getInt("rent_price_shared")));
        advanceAmount.setText(Long.toString(selectedRentHouse.getLong("advance")));
        advanceShared.setText(Long.toString(selectedRentHouse.getLong("advance_shared")));

    }

}

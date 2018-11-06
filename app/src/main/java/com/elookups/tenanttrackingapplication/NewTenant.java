package com.elookups.tenanttrackingapplication;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class NewTenant extends AppCompatActivity {

    private String imgName;
    private int id, tId;
    private int hi = 0;
    private boolean redirectToMain;

    private static java.util.Date joinDate;
    private static java.util.Date rentEffectiveFrom;

    private JSONArray rentHouseArray;
    private JSONObject selectedRentHouse = new JSONObject();

    private JSONArray tenantsArray;
    private JSONObject selectedTenant = new JSONObject();
    private JSONObject newTenant = new JSONObject();

    private String newTenantImagePath;
    private String newAttachImagePath;

    private JSONArray arrAttachImages;
    private JSONObject attachImageObject;

    private JSONArray arrRents;
    private JSONArray arrRentRates;

    private EditText newTenantName;
    private EditText newJoinDate;
    private EditText newTotalRentAgreed;
    private EditText newRentEffectiveFrom;
    private EditText newPreviousDues;
    private EditText newAdvanceReceived;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static class JoinDate  extends DialogFragment implements DatePickerDialog.OnDateSetListener {
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
            String temp = day + "/" + (month +1)  + "/" + year;
            try {
                joinDate = new SimpleDateFormat("dd/MM/yyyy").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            EditText newJoinDate = (EditText) getActivity().findViewById(R.id.newJoinDate);
            newJoinDate.setText(temp);
        }

    }

    public static class RentEffectiveFrom  extends DialogFragment implements DatePickerDialog.OnDateSetListener {
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
                rentEffectiveFrom = new SimpleDateFormat("dd/MM/yyyy").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            EditText newRentEffectiveFrom = (EditText) getActivity().findViewById(R.id.newRentEffectiveFrom);
            newRentEffectiveFrom.setText(temp);
        }

    }

    Bundle extraBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_tenant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        extraBundle = intentExtras.getExtras();

        final ImageView houseImages = (ImageView) findViewById(R.id.houseImages);
        TextView houseAddress = (TextView) findViewById(R.id.houseAddress);
        newAdvanceReceived = (EditText) findViewById(R.id.newAdvanceReceived);
        newTotalRentAgreed = (EditText) findViewById(R.id.newTotalRentAgreed);
        newJoinDate = (EditText) findViewById(R.id.newJoinDate);
        newRentEffectiveFrom = (EditText) findViewById(R.id.newRentEffectiveFrom);
        Button saveNewTenant = (Button) findViewById(R.id.saveNewTenant);
        Button updateNewTenant = (Button) findViewById(R.id.updateNewTenant);
        try {
            arrAttachImages = new JSONArray("[]");
            arrRents = new JSONArray("[]");
            arrRentRates = new JSONArray("[]");
            rentHouseArray = Misc.getRentHouseArray(NewTenant.this);
            id = extraBundle.getInt("id");
            selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);
            selectedRentHouse.put("rent_mode", extraBundle.getInt("rent_mode"));
            tenantsArray = selectedRentHouse.getJSONArray("tenants");
            houseImages.setImageBitmap(Misc.reducedBitmap(selectedRentHouse.getJSONArray("house_images").getJSONObject(0).getString("img_path"), 100, 100));
            houseAddress.setText(selectedRentHouse.getString("house_address"));
            if(extraBundle.getString("editMode").equals("addNewTenant")){
                newJoinDate.setText(Misc.todayString());
                newRentEffectiveFrom.setText(Misc.todayString());
                joinDate = Misc.todayDate();
                rentEffectiveFrom = Misc.todayDate();
                if(selectedRentHouse.getInt("rent_mode") == 1){
                    newAdvanceReceived.setText(selectedRentHouse.getString("advance"));
                    newTotalRentAgreed.setText(selectedRentHouse.getString("solo_rent_price"));
                }else{
                    newAdvanceReceived.setText(selectedRentHouse.getString("advance_shared"));
                    newTotalRentAgreed.setText(selectedRentHouse.getString("rent_price_shared"));
                }

                if (tenantsArray.length() == 0){
                    tId = 1;
                }else {
                    tId = tenantsArray.getJSONObject(tenantsArray.length() - 1).getInt("id")+1;
                }

            }else{
                setTitle("Edit Tenant Details");
                redirectToMain = true;
                tId = extraBundle.getInt("tId");
                selectedTenant = Misc.getSelectedObject(tId, tenantsArray);
                saveNewTenant.setVisibility(View.GONE);
                updateNewTenant.setVisibility(View.VISIBLE);
                joinDate = Misc.anyDate(selectedTenant.getString("join_date"));
                rentEffectiveFrom = Misc.anyDate(selectedTenant.getString("rent_effective_from"));
                refreshTenantInfo();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

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

        ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
        newTenantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgName = "tenant";
                dispatchTakePictureIntent();
            }
        });

        ImageView newAttachImage = (ImageView) findViewById(R.id.newAttachImage);
        newAttachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgName = "attach";
                dispatchTakePictureIntent();
            }
        });

        saveNewTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addNewTenant();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        updateNewTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    updateTenant();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        EditText newJoinDate = (EditText) findViewById(R.id.newJoinDate);
        newJoinDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new JoinDate();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        EditText newRentEffectiveFrom = (EditText) findViewById(R.id.newRentEffectiveFrom);
        newRentEffectiveFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new RentEffectiveFrom();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void dispatchTakePictureIntent() {

        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (imgName == "tenant"){
                    newTenantImagePath = Misc.getPathFromURI(uri, NewTenant.this);
                    ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
                    newTenantImage.setImageBitmap(Misc.reducedBitmap(newTenantImagePath, 100, 100));
//                    newTenantImage.setImageURI(uri);
//                    saveToInternalStorage(bitmap, "newTenantImage.jpg");
                }else if (imgName == "attach"){
                    newAttachImagePath = Misc.getPathFromURI(uri, NewTenant.this);
                    EditText newAttachName = (EditText) findViewById(R.id.newAttachName);
                    addImage(R.id.attchImages, newAttachImagePath, newAttachName.getText().toString(), imgName);
//                    ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
//                    newTenantImage.setImageBitmap(Misc.reducedBitmap(newAttachImagePath, 100, 100));
//                    newTenantImage.setImageURI(uri);
//                    saveToInternalStorage(bitmap, "newTenantImage.jpg");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addImage(final int id, String path, String text, final String type) throws JSONException {
        if(type.equals("house")){
//            houseImageObject = new JSONObject();
//            houseImageObject.put("img_path", path);
//            arrHouseImages.put(houseImageObject);
        }else if(type.equals("tenant")){
//            tenantImageObject = new JSONObject();
//            tenantImageObject.put("img_path", path);
//            tenantImageObject.put("tenant_name", text);
//            arrTenantImages.put(tenantImageObject);
        }else if(type.equals("attach")){
            attachImageObject = new JSONObject();
            attachImageObject.put("img_path", path);
            attachImageObject.put("attach_name", text);
            arrAttachImages.put(attachImageObject);
        }

        final LinearLayout imageList = (LinearLayout) findViewById(id);
        final float scale = this.getResources().getDisplayMetrics().density;
        int pixels = (int) (160 * scale + 0.5f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, pixels);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        int viewId;

        final RelativeLayout imageFrame = new RelativeLayout(this);
        viewId = Misc.uniqueViewId();
        imageFrame.setId(viewId);
        params.setMargins(0,0,0,16);

        ImageView newImage = new ImageView(this);
        viewId = Misc.uniqueViewId();
        newImage.setId(viewId);
        newImage.setOnLongClickListener(new View.OnLongClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public boolean onLongClick(View arg0) {
                if(type.equals("house")){
//                    arrHouseImages.remove(imageList.indexOfChild(imageFrame));
                }else if(type.equals("tenant")){
//                    arrTenantImages.remove(imageList.indexOfChild(imageFrame));
                }else if(type.equals("attach")){
                    try {
                        arrAttachImages = Misc.removeObjectAtI(arrAttachImages, imageList.indexOfChild(imageFrame));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                imageList.removeView(imageFrame);
                return true;    // <- set to true
            }
        });
        newImage.setScaleType(ImageView.ScaleType.FIT_XY);
        newImage.setImageBitmap(Misc.reducedBitmap(path, 100, 100));

        TextView textView = new TextView(this);
        viewId = Misc.uniqueViewId();
        textView.setId(viewId);
        textView.setText(text);
        params2.addRule(RelativeLayout.BELOW, newImage.getId());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
        textView.setTextColor(getResources().getColor(android.R.color.white));

        imageFrame.addView(newImage, params1);
        imageFrame.addView(textView, params2);

        imageList.addView(imageFrame, params);
    }

    private void addNewTenant() throws JSONException {

        JSONObject newTenant = new JSONObject();
        JSONObject newRentRate = new JSONObject();

        newTenantName = (EditText) findViewById(R.id.newTenantName);
        newJoinDate = (EditText) findViewById(R.id.newJoinDate);
        newTotalRentAgreed = (EditText) findViewById(R.id.newTotalRentAgreed);
        newRentEffectiveFrom = (EditText) findViewById(R.id.newRentEffectiveFrom);
        newPreviousDues = (EditText) findViewById(R.id.newPreviousDues);
        newAdvanceReceived = (EditText) findViewById(R.id.newAdvanceReceived);

        newRentRate.put("rent_effective_from", rentEffectiveFrom);
        newRentRate.put("total_rent_agreed", Long.parseLong(newTotalRentAgreed.getText().toString()));
        arrRentRates.put(newRentRate);

        newTenant.put("id", tId );
        newTenant.put("name", newTenantName.getText().toString());
        newTenant.put("img_path", newTenantImagePath);
        newTenant.put("attach_images", arrAttachImages);
        newTenant.put("rent_history", arrRents);
        newTenant.put("rent_rate_history", arrRentRates);
        newTenant.put("join_date", joinDate);
        newTenant.put("total_rent_agreed", Long.parseLong(newTotalRentAgreed.getText().toString()));
        newTenant.put("rent_effective_from", rentEffectiveFrom);
        newTenant.put("previous_dues", Long.parseLong(newPreviousDues.getText().toString()));
        newTenant.put("advance_received", Long.parseLong(newAdvanceReceived.getText().toString()));

        tenantsArray.put(newTenant);
        selectedRentHouse.put("tenants", tenantsArray);
        rentHouseArray = Misc.replaceObjectInArray(id, rentHouseArray, selectedRentHouse);
        Misc.writeToFile(rentHouseArray.toString(), "dataset.txt", NewTenant.this);
        Intent intentExtras = new Intent(NewTenant.this, RentHouseDetails.class);
        intentExtras.putExtra("id", id);
        startActivity(intentExtras);
        finish();

    }

    private void refreshTenantInfo() throws JSONException, ParseException {

        ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
        EditText newTenantName = (EditText) findViewById(R.id.newTenantName);
        newTenantImage.setImageBitmap(Misc.reducedBitmap(selectedTenant.getString("img_path"),100,100));
        newTenantName.setText(selectedTenant.getString("name"));

        for(int i=0; i<selectedTenant.getJSONArray("attach_images").length(); i++){
            addImage(R.id.attchImages, selectedTenant.getJSONArray("attach_images").getJSONObject(i).getString("img_path"), selectedTenant.getJSONArray("attach_images").getJSONObject(i).getString("attach_name"), "attach");
        }

        newJoinDate = (EditText) findViewById(R.id.newJoinDate);
        newTotalRentAgreed = (EditText) findViewById(R.id.newTotalRentAgreed);
        newRentEffectiveFrom = (EditText) findViewById(R.id.newRentEffectiveFrom);
        newPreviousDues = (EditText) findViewById(R.id.newPreviousDues);
        newAdvanceReceived = (EditText) findViewById(R.id.newAdvanceReceived);

        newJoinDate.setText(Misc.anyDateString(joinDate));
        newTotalRentAgreed.setText(Long.toString(selectedTenant.getLong("total_rent_agreed")));
        newRentEffectiveFrom.setText(Misc.anyDateString(rentEffectiveFrom));
        newPreviousDues.setText(Long.toString(selectedTenant.getLong("previous_dues")));
        newAdvanceReceived.setText(Long.toString(selectedTenant.getLong("advance_received")));

        arrRents = selectedTenant.getJSONArray("rent_history");
        arrRentRates = selectedTenant.getJSONArray("rent_rate_history");
    }

    private void updateTenant() throws JSONException, ParseException {
        JSONObject newTenant = new JSONObject();
        JSONObject newRentRate = new JSONObject();

        newTenantName = (EditText) findViewById(R.id.newTenantName);
        newJoinDate = (EditText) findViewById(R.id.newJoinDate);
        newTotalRentAgreed = (EditText) findViewById(R.id.newTotalRentAgreed);
        newRentEffectiveFrom = (EditText) findViewById(R.id.newRentEffectiveFrom);
        newPreviousDues = (EditText) findViewById(R.id.newPreviousDues);
        newAdvanceReceived = (EditText) findViewById(R.id.newAdvanceReceived);

        newRentRate.put("rent_effective_from", rentEffectiveFrom);
        newRentRate.put("total_rent_agreed", Long.parseLong(newTotalRentAgreed.getText().toString()));
        if(arrRentRates.length() == 0 || Misc.anyDate(arrRentRates.getJSONObject(arrRentRates.length()-1).getString("rent_effective_from")).before(rentEffectiveFrom) ){
            arrRentRates.put(newRentRate);
        }

        newTenant.put("id", tId );
        newTenant.put("name", newTenantName.getText().toString());
        newTenant.put("img_path", selectedTenant.getString("img_path"));
        newTenant.put("attach_images", arrAttachImages);
        newTenant.put("rent_history", arrRents);
        newTenant.put("rent_rate_history", arrRentRates);
        newTenant.put("join_date", joinDate);
        newTenant.put("total_rent_agreed", Long.parseLong(newTotalRentAgreed.getText().toString()));
        newTenant.put("rent_effective_from", rentEffectiveFrom);
        newTenant.put("previous_dues", Long.parseLong(newPreviousDues.getText().toString()));
        newTenant.put("advance_received", Long.parseLong(newAdvanceReceived.getText().toString()));

        tenantsArray = Misc.replaceObjectInArray(tId, tenantsArray, newTenant);
        selectedRentHouse.put("tenants", tenantsArray);
        rentHouseArray = Misc.replaceObjectInArray(id, rentHouseArray, selectedRentHouse);
        Misc.writeToFile(rentHouseArray.toString(), "dataset.txt", NewTenant.this);
        Intent intentExtras = new Intent(NewTenant.this, TenantDetails.class);
        intentExtras.putExtra("id", id);
        intentExtras.putExtra("tId", tId);
        intentExtras.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentExtras);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(extraBundle.getString("editMode").equals("addNewTenant")){
            Intent data = new Intent();
            data.putExtra("id", id);
            setResult(RESULT_OK);
            finish();
        }else{
            switch (item.getItemId()) {
                case android.R.id.home:
                    Intent data = new Intent(NewTenant.this, TenantDetails.class);
                    data.putExtra("id", id);
                    data.putExtra("tId", tId);
                    startActivity(data);
                    finish();
                    break;
            }
        }
        return true;
    }

}

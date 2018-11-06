package com.elookups.tenanttrackingapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class NewRentHouse extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String imgName;
    private int id;

    private JSONArray rentHouseArray;

    private String newRentHouseImagePath;
    private int rentHouseSize;

    private String availability;
    private int rent_mode;

    private JSONArray arrHouseImages;
    private JSONObject houseImageObject;

    private JSONArray arrTenants;
    private JSONObject tenantObject;
    private Bundle extraBundle;

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_rent_house);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        extraBundle = intentExtras.getExtras();
        Button saveNewRentHouse = (Button) findViewById(R.id.saveNewRentHouse);
        Button updateNewRentHouse = (Button) findViewById(R.id.updateNewRentHouse);
        try {
            arrHouseImages = new JSONArray("[]");
            arrTenants = new JSONArray("[]");
            rentHouseArray = Misc.getRentHouseArray(NewRentHouse.this);
            if(extraBundle.getString("editMode").equals("newRentHouse")){
                if (rentHouseArray.length() == 0){
                    id = 1;
                }else{
                    id = rentHouseArray.getJSONObject(rentHouseArray.length()-1).getInt("id")+1;
                }
            }else{
                setTitle("Edit Rent House");
                id = extraBundle.getInt("id");
                saveNewRentHouse.setVisibility(View.GONE);
                updateNewRentHouse.setVisibility(View.VISIBLE);
                refreshRentHouse();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ImageView newHouseImage = (ImageView) findViewById(R.id.newHouseImage);
        newHouseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgName = "house";
                dispatchTakePictureIntent();
            }
        });

        saveNewRentHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addRentHouse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        updateNewRentHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    updateRentHouse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Spinner houseSizeSpinner = (Spinner) findViewById(R.id.houseSizeSpinner);
        houseSizeSpinner.setOnItemSelectedListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos).toString()
        rentHouseSize = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        rentHouseSize = 1;
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
                if (imgName == "house"){
                    newRentHouseImagePath = Misc.getPathFromURI(uri, NewRentHouse.this);
                    addImage(R.id.newHouseImages, newRentHouseImagePath, "", imgName);
//                    ImageView newHouseImage = (ImageView) findViewById(R.id.newHouseImage);
//                    newHouseImage.setImageBitmap(reducedBitmap(newRentHouseImagePath, 100, 100));
//                    newHouseImage.setImageURI(uri);
//                    saveToInternalStorage(bitmap, "newHouseImage.jpg");
                }
//                else if (imgName == "tenant"){
//                    newTenantImagePath = getPathFromURI(uri);
//                    EditText newTenantName = (EditText) findViewById(R.id.newTenantName);
//                    addImage(R.id.tenantImages, newTenantImagePath, newTenantName.getText().toString(), imgName);
////                    ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
////                    newTenantImage.setImageBitmap(reducedBitmap(newTenantImagePath, 100, 100));
////                    newTenantImage.setImageURI(uri);
////                    saveToInternalStorage(bitmap, "newTenantImage.jpg");
//                }else if (imgName == "attach"){
//                    newAttachImagePath = getPathFromURI(uri);
//                    EditText newAttachName = (EditText) findViewById(R.id.newAttachName);
//                    addImage(R.id.attchImages, newAttachImagePath, newAttachName.getText().toString(), imgName);
////                    ImageView newTenantImage = (ImageView) findViewById(R.id.newTenantImage);
////                    newTenantImage.setImageBitmap(reducedBitmap(newAttachImagePath, 100, 100));
////                    newTenantImage.setImageURI(uri);
////                    saveToInternalStorage(bitmap, "newTenantImage.jpg");
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addImage(final int id, String path, String text, final String type) throws JSONException {
        if(type.equals("house")){
            houseImageObject = new JSONObject();
            houseImageObject.put("img_path", path);
            arrHouseImages.put(houseImageObject);
        }else if(type.equals("tenant")){
//            tenantObject = new JSONObject();
//            tenantObject.put("img_path", path);
//            tenantObject.put("tenant_name", text);
//            arrTenants.put(tenantObject);
        }else if(type.equals("attach")){
//            attachImageObject = new JSONObject();
//            attachImageObject.put("img_path", path);
//            attachImageObject.put("attach_name", path);
//            arrAttachImages.put(attachImageObject);
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
                try {
                    if(type.equals("house")){
                        arrHouseImages = Misc.removeObjectAtI(arrHouseImages,imageList.indexOfChild(imageFrame));
                    }else if(type.equals("tenant")){
    //                    arrTenants.remove(imageList.indexOfChild(imageFrame));
                    }else if(type.equals("attach")){
    //                    arrAttachImages.remove(imageList.indexOfChild(imageFrame));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    private void addRentHouse() throws JSONException {
        JSONObject newRentHouse = new JSONObject();

        EditText newHouseAddress = (EditText) findViewById(R.id.newHouseAddress);
        EditText newSoloRentPrice = (EditText) findViewById(R.id.newSoloRentPrice);
        EditText newSharedRentPrice = (EditText) findViewById(R.id.newSharedRentPrice);
        EditText newAdvance = (EditText) findViewById(R.id.newAdvance);
        EditText newSharedAdvance = (EditText) findViewById(R.id.newSharedAdvance);
        EditText newPersonsAllowed = (EditText) findViewById(R.id.newPersonsAllowed);
        CheckBox allowSharing = (CheckBox) findViewById(R.id.allowSharing);
        newRentHouse.put("id", id );
        newRentHouse.put("house_images", arrHouseImages);
        newRentHouse.put("tenants", arrTenants);
        newRentHouse.put("house_address", newHouseAddress.getText().toString());
        newRentHouse.put("size", rentHouseSize);
        newRentHouse.put("solo_rent_price", Long.parseLong(newSoloRentPrice.getText().toString()));
        newRentHouse.put("rent_price_shared", Long.parseLong(newSharedRentPrice.getText().toString()));
        newRentHouse.put("advance", Long.parseLong(newAdvance.getText().toString()));
        newRentHouse.put("advance_shared", Long.parseLong(newSharedAdvance.getText().toString()));
        newRentHouse.put("persons_allowed", Integer.parseInt(newPersonsAllowed.getText().toString()));
        newRentHouse.put("allow_sharing", allowSharing.isChecked());
        newRentHouse.put("availability", "Available");
        if (allowSharing.isChecked()){
            newRentHouse.put("rent_mode", 0);
        }else{
            newRentHouse.put("rent_mode", 1);
        }

        rentHouseArray.put(newRentHouse);
        Misc.writeToFile(rentHouseArray.toString(), "dataset.txt", NewRentHouse.this);
        Intent data = new Intent();
        data.putExtra("from", "NewRentHouse");
        setResult(RESULT_OK, data);
        finish();
    }

    private void refreshRentHouse()throws JSONException{
        JSONObject selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);

        Spinner houseSizeSpinner = (Spinner) findViewById(R.id.houseSizeSpinner);
        EditText newHouseAddress = (EditText) findViewById(R.id.newHouseAddress);
        EditText newSoloRentPrice = (EditText) findViewById(R.id.newSoloRentPrice);
        EditText newSharedRentPrice = (EditText) findViewById(R.id.newSharedRentPrice);
        EditText newAdvance = (EditText) findViewById(R.id.newAdvance);
        EditText newSharedAdvance = (EditText) findViewById(R.id.newSharedAdvance);
        EditText newPersonsAllowed = (EditText) findViewById(R.id.newPersonsAllowed);
        CheckBox allowSharing = (CheckBox) findViewById(R.id.allowSharing);

        newHouseAddress.setText(selectedRentHouse.getString("house_address"));
        houseSizeSpinner.setSelection(selectedRentHouse.getInt("size"));
        newSoloRentPrice.setText(Long.toString(selectedRentHouse.getLong("solo_rent_price")));
        newSharedRentPrice.setText(Long.toString(selectedRentHouse.getLong("rent_price_shared")));
        newAdvance.setText(Long.toString(selectedRentHouse.getLong("advance")));
        newSharedAdvance.setText(Long.toString(selectedRentHouse.getLong("advance_shared")));
        newPersonsAllowed.setText(Integer.toString(selectedRentHouse.getInt("persons_allowed")));
        allowSharing.setChecked(selectedRentHouse.getBoolean("allow_sharing"));

        availability = selectedRentHouse.getString("availability");
        rent_mode = selectedRentHouse.getInt("rent_mode");
        arrTenants = selectedRentHouse.getJSONArray("tenants");

        for(int i=0; i<selectedRentHouse.getJSONArray("house_images").length(); i++){
            addImage(R.id.newHouseImages, selectedRentHouse.getJSONArray("house_images").getJSONObject(i).getString("img_path"), "", "house");
        }

    }

    private void updateRentHouse() throws JSONException {
        JSONObject newRentHouse = new JSONObject();

        EditText newHouseAddress = (EditText) findViewById(R.id.newHouseAddress);
        EditText newSoloRentPrice = (EditText) findViewById(R.id.newSoloRentPrice);
        EditText newSharedRentPrice = (EditText) findViewById(R.id.newSharedRentPrice);
        EditText newAdvance = (EditText) findViewById(R.id.newAdvance);
        EditText newSharedAdvance = (EditText) findViewById(R.id.newSharedAdvance);
        EditText newPersonsAllowed = (EditText) findViewById(R.id.newPersonsAllowed);
        CheckBox allowSharing = (CheckBox) findViewById(R.id.allowSharing);

        newRentHouse.put("id", id );
        newRentHouse.put("house_images", arrHouseImages);
        newRentHouse.put("tenants", arrTenants);
        newRentHouse.put("house_address", newHouseAddress.getText().toString());
        newRentHouse.put("size", rentHouseSize);
        newRentHouse.put("solo_rent_price", Long.parseLong(newSoloRentPrice.getText().toString()));
        newRentHouse.put("rent_price_shared", Long.parseLong(newSharedRentPrice.getText().toString()));
        newRentHouse.put("advance", Long.parseLong(newAdvance.getText().toString()));
        newRentHouse.put("advance_shared", Long.parseLong(newSharedAdvance.getText().toString()));
        newRentHouse.put("persons_allowed", Integer.parseInt(newPersonsAllowed.getText().toString()));
        newRentHouse.put("allow_sharing", allowSharing.isChecked());

        newRentHouse.put("availability", availability);
        if (allowSharing.isChecked()){
            if (arrHouseImages.length() == 0){
                newRentHouse.put("rent_mode", 0);
            }else{
                newRentHouse.put("rent_mode", rent_mode);
            }
        }else{
            newRentHouse.put("rent_mode", 1);
        }

        rentHouseArray = Misc.replaceObjectInArray(id, rentHouseArray, newRentHouse);

        Misc.writeToFile(rentHouseArray.toString(),"dataset.txt", NewRentHouse.this);
        Intent intentExtras = new Intent(NewRentHouse.this, RentHouseDetails.class);
        intentExtras.putExtra("id", id);
        intentExtras.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentExtras);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(extraBundle.getString("editMode").equals("newRentHouse")){
            setResult(RESULT_OK);
            finish();
        }else{
            switch (item.getItemId()) {
                case android.R.id.home:
                    Intent data = new Intent(NewRentHouse.this, RentHouseDetails.class);
                    data.putExtra("id", id);
                    startActivity(data);
                    finish();
                    break;
            }
        }
        return true;
    }
}

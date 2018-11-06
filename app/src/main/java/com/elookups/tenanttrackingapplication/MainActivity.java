package com.elookups.tenanttrackingapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    private class Tenant implements Comparable<Tenant>{
        int id;
        int tId;
        String img_path;
        String tenant_name;
        long pending_due;

        public int compareTo(Tenant other) {
            return this.pending_due<other.pending_due?1:
                    this.pending_due>other.pending_due?-1:0;
        }
    }

    private class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.MyViewHolder> {

        private ArrayList<Tenant> tenants;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView heading_txt, text_value, pending_due_hd, pending_due_val;
            public ImageView img_view;

            public MyViewHolder(View view) {
                super(view);
                img_view = (ImageView) view.findViewById(R.id.img_view);
                heading_txt = (TextView) view.findViewById(R.id.heading_txt);
                text_value = (TextView) view.findViewById(R.id.text_value);
                pending_due_hd = (TextView) view.findViewById(R.id.pending_due_hd);
                pending_due_val = (TextView) view.findViewById(R.id.pending_due_val);
            }
        }

        public TenantAdapter(ArrayList<Tenant> tenants) {
            this.tenants = tenants;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_view_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Tenant tenant = tenants.get(position);
            holder.text_value.setText(tenant.tenant_name);
            holder.heading_txt.setText("Tenant Name:-");
            holder.pending_due_val.setText(Long.toString(tenant.pending_due));
            holder.pending_due_hd.setText("Pending Due:-");
//                imageLoader.DisplayImage(getItem(pos).img_path, img_view);
            holder.img_view.setImageBitmap (Misc.reducedBitmap(tenant.img_path, 100, 100));
            final int id, tId;
            id = tenant.id;
            tId = tenant.tId;
            holder.img_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentExtras = new Intent(MainActivity.this, TenantDetails.class);
                    intentExtras.putExtra("id", id);
                    intentExtras.putExtra("tId", tId);
                    startActivity(intentExtras);
                }
            });
        }

        @Override
        public int getItemCount() {
            return tenants.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        try {
            populateListView();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intentExtras = new Intent(MainActivity.this, NewRentHouse.class);
            intentExtras.putExtra("editMode", "newRentHouse");
            startActivity(intentExtras);
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(MainActivity.this, RentHouseList.class));
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(MainActivity.this, PinChangeActivity.class));
        } else if (id == R.id.import_app_data) {
            Misc.writeToFile(Misc.readFromExternalFolder(MainActivity.this),"dataset.txt", MainActivity.this);
        } else if (id == R.id.export_app_data) {
            try {
                Misc.saveToExternalFolder(Misc.getRentHouseArray(MainActivity.this).toString(), MainActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void populateListView() throws JSONException {
        JSONArray rentHouseArray = Misc.getRentHouseArray(MainActivity.this);
        int noOfTenants = 1;
        ArrayList<Tenant> tenants = new ArrayList<Tenant>();
        Tenant tenant;
        for (int i=0; i<rentHouseArray.length(); i++){
            noOfTenants = rentHouseArray.getJSONObject(i).getJSONArray("tenants").length();
            for (int j = 0; j < noOfTenants; j++) {
                tenant = new Tenant();
                tenant.id = rentHouseArray.getJSONObject(i).getInt("id");
                tenant.tId = rentHouseArray.getJSONObject(i).getJSONArray("tenants").getJSONObject(j).getInt("id");
                tenant.img_path = rentHouseArray.getJSONObject(i).getJSONArray("tenants").getJSONObject(j).getString("img_path");
                tenant.tenant_name = rentHouseArray.getJSONObject(i).getJSONArray("tenants").getJSONObject(j).getString("name");
                tenant.pending_due = rentHouseArray.getJSONObject(i).getJSONArray("tenants").getJSONObject(j).getLong("previous_dues");
                tenants.add(tenant);
            }
        }
        Collections.sort(tenants);

        RecyclerView tenantRecycler = (RecyclerView) findViewById(R.id.tenantRecycler);
        tenantRecycler.setLayoutManager(new LinearLayoutManager(this));
//        tenantRecycler.setItemAnimator(new DefaultItemAnimator());
        TenantAdapter tenantAdapter = new TenantAdapter(tenants);
        tenantRecycler.setAdapter(tenantAdapter);
//        tenantAdapter.notifyDataSetChanged();
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        startActivityForResult(photoPickerIntent, 1);

//       Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//       if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//           startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//       }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
////                ...
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                Uri photoURI = FileProvider.getUriForFile(this,
//                        "com.example.android.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            ImageView bikeImage = (ImageView) view.findViewById(R.id.imageView3);
//            bikeImage.setImageBitmap(imageBitmap);
//        }
//    }

//    @Override
//    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
//        super.onActivityResult(reqCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            try {
//                final Uri imageUri = data.getData();
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//
//                final ImageView bv = (ImageView) findViewById(R.id.Logo);
//                bv.setImageBitmap(selectedImage);
//
////                ImageView bikeImage = (ImageView) view.findViewById(R.id.houseImage1);
////                bikeImage.setImageBitmap(selectedImage);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
////                Toast.makeText(PostImage.this, "Something went wrong", Toast.LENGTH_LONG).show();
//            }
//
//        } else {
////            Toast.makeText(PostImage.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            Uri uri = data.getData();
//
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                // Log.d(TAG, String.valueOf(bitmap));
//
//                ImageView imageView = (ImageView) findViewById(R.id.houseImage1);
//                imageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void onDestroy()
    {
        // Remove adapter refference from list
//        list.setAdapter(null);
        super.onDestroy();
    }

    public View.OnClickListener listener=new View.OnClickListener(){
        @Override
        public void onClick(View arg0) {

            //Refresh cache directory downloaded images
//            adapter.imageLoader.clearCache();
//            adapter.notifyDataSetChanged();
        }
    };


    public void onItemClick(int mPosition)
    {
        String tempValues = mStrings[mPosition];

        Toast.makeText(MainActivity.this,
                "Image URL : "+tempValues,
                Toast.LENGTH_LONG).show();
    }

    // Image urls used in LazyImageLoadAdapter.java file

    private String[] mStrings={
            "/media/webservice/LazyListView_images/image0.png",
            "/media/webservice/LazyListView_images/image1.png",
            "/media/webservice/LazyListView_images/image2.png",
            "/media/webservice/LazyListView_images/image3.png",
            "/media/webservice/LazyListView_images/image4.png",
            "/media/webservice/LazyListView_images/image5.png",
            "/media/webservice/LazyListView_images/image6.png",
            "/media/webservice/LazyListView_images/image7.png",
            "/media/webservice/LazyListView_images/image8.png",
            "/media/webservice/LazyListView_images/image9.png",
            "/media/webservice/LazyListView_images/image10.png",
            "/media/webservice/LazyListView_images/image0.png",
            "/media/webservice/LazyListView_images/image1.png",
            "/media/webservice/LazyListView_images/image2.png",
            "/media/webservice/LazyListView_images/image3.png",
            "/media/webservice/LazyListView_images/image4.png",
            "/media/webservice/LazyListView_images/image5.png",
            "/media/webservice/LazyListView_images/image6.png",
            "/media/webservice/LazyListView_images/image7.png",
            "/media/webservice/LazyListView_images/image8.png",
            "/media/webservice/LazyListView_images/image9.png",
            "/media/webservice/LazyListView_images/image10.png",
            "/media/webservice/LazyListView_images/image0.png",
            "/media/webservice/LazyListView_images/image1.png",
            "/media/webservice/LazyListView_images/image2.png",
            "/media/webservice/LazyListView_images/image3.png",
            "/media/webservice/LazyListView_images/image4.png",
            "/media/webservice/LazyListView_images/image5.png",
            "/media/webservice/LazyListView_images/image6.png",
            "/media/webservice/LazyListView_images/image7.png",
            "/media/webservice/LazyListView_images/image8.png",
            "/media/webservice/LazyListView_images/image9.png",
            "/media/webservice/LazyListView_images/image10.png"

    };
//
//    mGparser = new JsonParser();
//    Gson mGson = new Gson();
//
//    Url url = "http://your_api.com"
//    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//    conn.setRequestProperty("Connection", "close");
//    conn.connect();
//    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//
//    JsonArray request = (JsonArray) mGparser.parse(in.readLine());
//    in.close();
//    ArrayList<MyArrayAdapterItem> items = mGson.fromJson(request, new TypeToken<ArrayList<MyArrayAdapterItem>>() {}.getType());

    //
//    private class ListAdapter extends ArrayAdapter<ListItem> {
//        public View v;
//        public ImageLoader imageLoader;
//        public ListAdapter(Context context, int resource, ArrayList<ListItem> array){
//            super(context, resource, array);
//            imageLoader = new ImageLoader(context);
//        }
//
//        @Override
//        public View getView(int pos, View convertView, ViewGroup parent){
//            this.v = convertView;
//            if(v==null) {
//                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v=vi.inflate(R.layout.list_view_item, null);
//            }
//
//            ImageView img_view = (ImageView)v.findViewById(R.id.img_view);
//            TextView heading_txt = (TextView)v.findViewById(R.id.heading_txt);
//            TextView text_value = (TextView)v.findViewById(R.id.text_value);
//            text_value.setText(getItem(pos).text_value);
//            if (listState.equals("tenants")){
//                heading_txt.setText("Tenant Name:-");
////                imageLoader.DisplayImage(getItem(pos).img_path, img_view);
//                img_view.setImageBitmap(Misc.reducedBitmap(getItem(pos).img_path, 100, 100));
//                final int id, tId;
//                id = getItem(pos).id;
//                tId = getItem(pos).tId;
//                img_view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intentExtras = new Intent(MainActivity.this, TenantDetails.class);
//                        intentExtras.putExtra("id", id);
//                        intentExtras.putExtra("tId", tId);
//                        startActivity(intentExtras);
//                    }
//                });
//            }else{
//                heading_txt.setText("House Address:-");
////                imageLoader.DisplayImage(getItem(pos).img_path, img_view);
//                img_view.setImageBitmap(Misc.reducedBitmap(getItem(pos).img_path, 100, 100));
//                final int id;
//                id = getItem(pos).id;
//                img_view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intentExtras = new Intent(MainActivity.this, RentHouseDetails.class);
//                        intentExtras.putExtra("id", id);
//                        startActivity(intentExtras);
//                    }
//                });
//            }
//
//            return v;
//        }
//    }

}

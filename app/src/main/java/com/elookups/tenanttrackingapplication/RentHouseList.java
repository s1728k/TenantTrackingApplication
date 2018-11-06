package com.elookups.tenanttrackingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class RentHouseList extends AppCompatActivity {

    private class RentHouse{
        int id;
        String img_path;
        String address;
    }

    private class RentHouseAdapter extends RecyclerView.Adapter<RentHouseAdapter.MyViewHolder> {

        private ArrayList<RentHouse> rentHouses;

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


        public RentHouseAdapter(ArrayList<RentHouse> rentHouses) {
            this.rentHouses = rentHouses;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_view_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            RentHouse rentHouse = rentHouses.get(position);
            holder.text_value.setText(rentHouse.address);
            holder.heading_txt.setText("House Address:-");
            holder.pending_due_hd.setText("");
            holder.pending_due_val.setText("");
//            imageLoader.DisplayImage(getItem(pos).img_path, img_view);
            holder.img_view.setImageBitmap (Misc.reducedBitmap(rentHouse.img_path, 100, 100));
            final int id;
            id = rentHouse.id;
            holder.img_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentExtras = new Intent(RentHouseList.this, RentHouseDetails.class);
                    intentExtras.putExtra("id", id);
                    startActivity(intentExtras);
                }
            });
        }

        @Override
        public int getItemCount() {
            return rentHouses.size();
        }
    }

    private ArrayList<String> houseSizes = new ArrayList<>();
    private String availability = "allRentHouses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        Bundle extraBundle = intentExtras.getExtras();
        if(!intentExtras.hasExtra("availabilityFilter")) {
            for (int i = 0; i < 5; i++) {
                houseSizes.add(Integer.toString(i));
            }
        }else{
            availability = extraBundle.getString("availabilityFilter");
            if(extraBundle.getBoolean("studio")){
                houseSizes.add(Integer.toString(0));
            }
            if(extraBundle.getBoolean("1bhk")){
                houseSizes.add(Integer.toString(1));
            }
            if(extraBundle.getBoolean("2bhk")){
                houseSizes.add(Integer.toString(2));
            }
            if(extraBundle.getBoolean("3bhk")){
                houseSizes.add(Integer.toString(3));
            }
            if(extraBundle.getBoolean("mbhk")){
                houseSizes.add(Integer.toString(4));
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RentHouseList.this, FilterHouse.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            populateListView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void populateListView() throws JSONException {
        JSONArray rentHouseArray = Misc.getRentHouseArray(RentHouseList.this);
        ArrayList<RentHouse> rentHouses = new ArrayList<>();
        RentHouse rentHouse;
        for (int i=0; i<rentHouseArray.length(); i++){
            if(houseSizes.contains(Integer.toString(rentHouseArray.getJSONObject(i).getInt("size")))){
                if(availability.equals("allRentHouses")) {
                }else if(availability.equals("availableForFamily")) {
                    if(rentHouseArray.getJSONObject(i).getJSONArray("tenants").length()==0){
                    }else{
                        continue;
                    }
                }else if(availability.equals("availableForSharing")) {
                    if(rentHouseArray.getJSONObject(i).getJSONArray("tenants").length()< rentHouseArray.getJSONObject(i).getInt("persons_allowed") && rentHouseArray.getJSONObject(i).getBoolean("allow_sharing")){
                    }else{
                        continue;
                    }
                }else if(availability.equals("partlyFilledShared")) {
                    if(rentHouseArray.getJSONObject(i).getJSONArray("tenants").length()< rentHouseArray.getJSONObject(i).getInt("persons_allowed") && rentHouseArray.getJSONObject(i).getJSONArray("tenants").length()!=0){
                    }else{
                        continue;
                    }
                }
                rentHouse = new RentHouse();
                rentHouse.id = rentHouseArray.getJSONObject(i).getInt("id");
                rentHouse.img_path = rentHouseArray.getJSONObject(i).getJSONArray("house_images").getJSONObject(0).getString("img_path");
                rentHouse.address = rentHouseArray.getJSONObject(i).getString("house_address");
                rentHouses.add(rentHouse);
            }
        }
        RecyclerView renthouseRecycler = (RecyclerView) findViewById(R.id.renthouseRecycler);
        renthouseRecycler.setLayoutManager(new LinearLayoutManager(this));
//        renthouseRecycler.setItemAnimator(new DefaultItemAnimator());
        RentHouseAdapter rentHouseAdapter = new RentHouseAdapter(rentHouses);
        renthouseRecycler.setAdapter(rentHouseAdapter);
//        rentHouseAdapter.notifyDataSetChanged();
    }

}

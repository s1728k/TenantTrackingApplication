package com.elookups.tenanttrackingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

/**
 * Created by s1728 on 10/27/2017.
 */
public class FilterHouse extends AppCompatActivity {

    private String availabilityFilter = "allRentHouses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_houses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button applyHouseFilter = (Button) findViewById(R.id.applyHouseFilter);
        applyHouseFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox studioCheck = (CheckBox) findViewById(R.id.studioCheck);
                CheckBox oneBhkCheck = (CheckBox) findViewById(R.id.oneBhkCheck);
                CheckBox twoBhkCheck = (CheckBox) findViewById(R.id.twoBhkCheck);
                CheckBox threeBhkCheck = (CheckBox) findViewById(R.id.threeBhkCheck);
                CheckBox moreBhkCheck = (CheckBox) findViewById(R.id.moreBhkCheck);

                Intent intentExtras = new Intent(FilterHouse.this, RentHouseList.class);
                intentExtras.putExtra("studio", studioCheck.isChecked());
                intentExtras.putExtra("1bhk", oneBhkCheck.isChecked());
                intentExtras.putExtra("2bhk", twoBhkCheck.isChecked());
                intentExtras.putExtra("3bhk", threeBhkCheck.isChecked());
                intentExtras.putExtra("mbhk", moreBhkCheck.isChecked());
                intentExtras.putExtra("availabilityFilter", availabilityFilter);

                startActivity(intentExtras);
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void availabilityRadio(View view){
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.allRentHouses:
                if (checked)
                    availabilityFilter = "allRentHouses";
                    break;
            case R.id.availableForFamily:
                if (checked)
                    availabilityFilter = "availableForFamily";
                    break;
            case R.id.availableForSharing:
                if (checked)
                    availabilityFilter = "availableForSharing";
                    break;
            case R.id.partlyFilledShared:
                if (checked)
                    availabilityFilter = "partlyFilledShared";
                    break;
        }
    }
}

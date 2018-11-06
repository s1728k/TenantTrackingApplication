package com.elookups.tenanttrackingapplication;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class PinChangeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_change);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final AutoCompleteTextView new_pin = (AutoCompleteTextView) findViewById(R.id.new_pin);
        Button pin_change_button = (Button) findViewById(R.id.pin_change_button);
        pin_change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(Misc.readFromFile("activation.txt", PinChangeActivity.this));
                    jsonObject.put("pin", new_pin.getText().toString());
                    Misc.writeToFile(jsonObject.toString(), "activation.txt", PinChangeActivity.this);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}

package com.elookups.tenanttrackingapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;

/**
 * Created by s1728 on 10/27/2017.
 */
public class RentHistory extends AppCompatActivity {

    private int id=0;
    private int tId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rent_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intentExtras = getIntent();
        Bundle extraBundle = intentExtras.getExtras();
        id = extraBundle.getInt("id");
        tId = extraBundle.getInt("tId");
        String displayParam = extraBundle.getString("displayParam");
        if (displayParam.equals("rent_payment_history")){
            setTitle("Rent Payment History");
        }else{
            setTitle("Rent Rate History");
        }

        try {
            rentPaymentHistory(displayParam, id, tId);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void rentPaymentHistory(String displayParam, int id, int tId) throws JSONException, ParseException {
        JSONArray rentHouseArray = Misc.getRentHouseArray(RentHistory.this);
        JSONObject selectedRentHouse = Misc.getSelectedObject(id, rentHouseArray);
        JSONArray tenantsArray = selectedRentHouse.getJSONArray("tenants");
        JSONObject selectedTenant = Misc.getSelectedObject(tId, tenantsArray);
        TableLayout rentHistoryTable = (TableLayout) findViewById(R.id.rentHistoryTable);

        JSONArray jsonArray;
        if (displayParam.equals("rent_payment_history")){
            jsonArray = selectedTenant.getJSONArray("rent_history");
        }else{
            jsonArray = selectedTenant.getJSONArray("rent_rate_history");
            TextView ammountHistory = (TextView) findViewById(R.id.ammountHistory);
            ammountHistory.setText("Rent Rate");
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            TableRow tr = new TableRow(this);

            TextView fc = new TextView(this);
            fc.setLayoutParams(new TableRow.LayoutParams(0));
            fc.setTextColor(Color.WHITE);
            fc.setTextSize(16);
            fc.setGravity(Gravity.LEFT);

            TextView sc = new TextView(this);
            sc.setLayoutParams(new TableRow.LayoutParams(1));
            sc.setTextColor(Color.WHITE);
            sc.setTextSize(16);
            sc.setGravity(Gravity.LEFT);

            if (displayParam.equals("rent_payment_history")){
                fc.setText(Misc.anyDateString(Misc.anyDate(jsonArray.getJSONObject(i).getString("received_date"))));
                sc.setText(jsonArray.getJSONObject(i).getString("amount_paid"));
            }else{
                fc.setText(Misc.anyDateString(Misc.anyDate(jsonArray.getJSONObject(i).getString("rent_effective_from"))));
                sc.setText(jsonArray.getJSONObject(i).getString("total_rent_agreed"));
            }

            tr.addView(fc);
            tr.addView(sc);

            rentHistoryTable.addView(tr);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra("id", id);
                data.putExtra("tId", tId);
                setResult(RESULT_OK, data);
                finish();
                break;
        }
        return true;
    }

}

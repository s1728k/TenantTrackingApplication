package com.elookups.tenanttrackingapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class PinLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        JSONObject jsonObject;
        TextView activation_status = (TextView) findViewById(R.id.activation_status);
        final TextView pin_login_err = (TextView) findViewById(R.id.pin_login_err);

        try {
            jsonObject = new JSONObject(Misc.readFromFile("activation.txt", PinLoginActivity.this));
            if(jsonObject.getString("status").equals("success") && jsonObject.getString("hdd").equals(Misc.getAndroidID(PinLoginActivity.this))){
                activation_status.setText("Activated");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final AutoCompleteTextView pin = (AutoCompleteTextView) findViewById(R.id.pin);
        Button pin_sign_in_button = (Button) findViewById(R.id.pin_sign_in_button);
        pin_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String activationFile = Misc.readFromFile("activation.txt", PinLoginActivity.this);
                try {
                    JSONObject jsonObject = new JSONObject(activationFile);
                    if(jsonObject.getString("hdd").equals(null)){
                    }else{
                        if (pin.getText().toString().equals("123456") && jsonObject.getString("pin").equals("") || pin.getText().toString().equals(jsonObject.getString("pin"))){
                            pin_login_err.setVisibility(View.GONE);
                            startActivity(new Intent(PinLoginActivity.this, MainActivity.class));
                        }else{
                            pin_login_err.setVisibility(View.VISIBLE);
                            pin_login_err.setText("Err: Incorrect PIN");
                        }
                    }
                } catch (JSONException e) {
                    pin_login_err.setVisibility(View.VISIBLE);
                    pin_login_err.setText("Err: Not Activated");
                    e.printStackTrace();
                }
            }
        });

        Button license_activate_button = (Button) findViewById(R.id.license_activate_button);
        license_activate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LicenseActivation().execute("Activate");
            }
        });

        Button license_deactivate_button = (Button) findViewById(R.id.license_deactivate_button);
        license_deactivate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LicenseActivation().execute("Deactivate");
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private class LicenseActivation extends AsyncTask<String, Boolean, String> {
        protected String doInBackground(String... params) {
            String ret ="Not Activated";
//            publishProgress((int) ((i / (float) count) * 100));
            publishProgress(true);
            JSONObject jsonParam = new JSONObject();
            JSONObject jsonRes = new JSONObject();
            // Create URL
            URL activateUrl = null;
            try {
                if(params[0].equals("Activate")){
                    activateUrl = new URL("https://cblms.000webhostapp.com/license/activate");
                }else if(params[0].equals("Deactivate")){
                    activateUrl = new URL("https://cblms.000webhostapp.com/license/deactivate");
                }
                // Create connection
                HttpsURLConnection myConnection = null;
                assert activateUrl != null;
                myConnection = (HttpsURLConnection) activateUrl.openConnection();
                assert myConnection != null;
                myConnection.setConnectTimeout(5000);
                myConnection.setRequestMethod("POST");
                myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                myConnection.setRequestProperty("Content-type", "application/application/json");
                myConnection.setRequestProperty("Accept", "application/josn");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setInstanceFollowRedirects(false);
                myConnection.getRequestMethod();
                jsonParam.put("license_key", "b8c99a053416544942ad527f41749aa9");
                jsonParam.put("hardware_code", Misc.getAndroidID(PinLoginActivity.this));
                if(params[0].equals("Activate")){
                    jsonParam.put("computer_user", "mobile_user");
                    jsonParam.put("computer_name", "mobile_user");
                }
                DataOutputStream printout = new DataOutputStream(myConnection.getOutputStream ());
                printout.writeBytes(jsonParam.toString());
                printout.flush ();
                printout.close ();
//                OutputStreamWriter out = new OutputStreamWriter(myConnection.getOutputStream());
//                out.write(jsonParam.toString());
//                out.close();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader (responseBodyReader);
                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        jsonRes.put(jsonReader.nextName(), jsonReader.nextString());
//                        jsonReader.skipValue();
                    }
                    jsonReader.close();
                    myConnection.disconnect();
                    if(jsonRes.getString("status").equals("success")){
                        if(params[0].equals("Activate")){
                            jsonRes.put("hdd", Misc.getAndroidID(PinLoginActivity.this));
                            jsonRes.put("pin", "123456");
                            ret = "Activated";
                        }else{
                            ret = "Dectivated";
                        }
                        Misc.writeToFile(jsonRes.toString(), "activation.txt", PinLoginActivity.this);
                    }
                }else{
                    if(params[0].equals("Activate")){
                        activateUrl = new URL("https://cblms2.000webhostapp.com/license/activate");
                    }else if(params[0].equals("Deactivate")){
                        activateUrl = new URL("https://cblms2.000webhostapp.com/license/deactivate");
                    }
                    // Create connection
                    assert activateUrl != null;
                    myConnection = (HttpsURLConnection) activateUrl.openConnection();
                    assert myConnection != null;
                    myConnection.setConnectTimeout(5000);
                    myConnection.setRequestMethod("POST");
                    myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");
                    myConnection.setRequestProperty("Content-type", "application/application/json");
                    myConnection.setRequestProperty("Accept", "application/josn");
                    myConnection.setDoInput(true);
                    myConnection.setDoOutput(true);
                    myConnection.setInstanceFollowRedirects(false);
                    myConnection.getRequestMethod();
                    jsonParam.put("license_key", "b8c99a053416544942ad527f41749aa9");
                    jsonParam.put("hardware_code", Misc.getAndroidID(PinLoginActivity.this));
                    if(params[0].equals("Activate")){
                        jsonParam.put("computer_user", "mobile_user");
                        jsonParam.put("computer_name", "mobile_user");
                    }
                    printout = new DataOutputStream(myConnection.getOutputStream ());
                    printout.writeBytes(jsonParam.toString());
                    printout.flush ();
                    printout.close ();

                    if (myConnection.getResponseCode() == 200) {
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader (responseBodyReader);
                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            jsonRes.put(jsonReader.nextName(), jsonReader.nextString());
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                        if(jsonRes.getString("status").equals("success")){
                            if(params[0].equals("Activate")){
                                jsonRes.put("hdd", Misc.getAndroidID(PinLoginActivity.this));
                                jsonRes.put("pin", "123456");
                                ret = "Activated";
                            }else{
                                ret = "Dectivated";
                            }
                            Misc.writeToFile(jsonRes.toString(), "activation.txt", PinLoginActivity.this);
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }catch (JSONException e) {
                e.printStackTrace();
            }
            publishProgress(false);
            return ret;
        }

        protected void onProgressUpdate(Boolean... wait) {
            if(wait[0]){
                setActivationStatus("Wait...");
            }
        }

        protected void onPostExecute(String status) {
            setActivationStatus(status);
        }
    }

    public void setActivationStatus(String status){
        TextView activation_status = (TextView) findViewById(R.id.activation_status);
        activation_status.setText(status);
    }

}

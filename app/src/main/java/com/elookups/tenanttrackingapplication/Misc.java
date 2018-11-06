package com.elookups.tenanttrackingapplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.test.AndroidTestCase;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by s1728 on 10/25/2017.
 */
public class Misc {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static JSONArray getRentHouseArray(Context context) throws JSONException {
        String filename = "dataset.txt";
        String jsonString = readFromFile(filename, context);
        JSONArray jsonData;
        if(jsonString != ""){
            jsonData = new JSONArray(jsonString);
        }else{
            jsonData = new JSONArray("[]");
        }
        return jsonData;
    }

    public static JSONObject getSelectedObject(int id, JSONArray jsonArray) throws JSONException {
        JSONObject object = new JSONObject();
        for (int i=id-1; i>=0; i--){
            if (id == jsonArray.getJSONObject(i).getInt("id")){
                object = jsonArray.getJSONObject(i);
                break;
            }
        }
        return object;
    }

    public static JSONArray replaceObjectInArray(int id, JSONArray jsonArray, JSONObject jsonObject) throws JSONException {
        for (int i=id-1; i>=0; i--){
            if (id == jsonArray.getJSONObject(i).getInt("id")){
                jsonArray.put(i, jsonObject);
                break;
            }
        }
        return jsonArray;
    }

    public static JSONArray removeObjectInArray(int id, JSONArray jsonArray) throws JSONException {
        for (int i=id-1; i>=0; i--){
            if (id == jsonArray.getJSONObject(i).getInt("id")){
                Misc.removeObjectAtI(jsonArray, i);
                break;
            }
        }
        return jsonArray;
    }

    public static String readFromFile(String filename, Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);
//            InputStream inputStream =  openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public static void writeToFile(String data, String filename, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static Bitmap reducedBitmap(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static JSONArray sortedJSONArray(JSONArray jsonArr) throws JSONException {
        //I assume that we need to create a JSONArray object from the following string
//        String jsonArrStr = "[ { \"ID\": \"135\", \"Name\": \"Fargo Chan\" },{ \"ID\": \"432\", \"Name\": \"Aaron Luke\" },{ \"ID\": \"252\", \"Name\": \"Dilip Singh\" }]";

//        JSONArray jsonArr = new JSONArray(jsonArrStr);
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID
            private static final String KEY_NAME = "Name";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                return valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        return sortedJsonArray;
    }

    public static int uniqueViewId(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    public static String saveToInternalStorage(Bitmap bitmapImage, String ImgName, Context context){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, ImgName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

//    public static Bitmap loadImageFromStorage(String path)
//    {
//        try {
//            File f=new File(path, "profile.jpg");
//            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//            return b;
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        return new Bitmap();
//    }

    public static String getPathFromURI(Uri contentUri, Context context) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            res = contentUri.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
                cursor.close();
            }
        }
        return res;
    }

    public static JSONArray removeObjectAtI(JSONArray jsonArray, int position) throws JSONException {
        JSONArray list = new JSONArray();
        int len = jsonArray.length();
        if (jsonArray != null) {
            for (int i=0;i<len;i++)
            {
                //Excluding the item at position
                if (i != position)
                {
                    list.put(jsonArray.get(i));
                }
            }
        }
        return list;
    }

    public static int dateItemToday(String item){
        Calendar cal = Calendar.getInstance();
        switch (item){
            case "day":
                return cal.get(Calendar.DAY_OF_MONTH);
            case "month":
                return cal.get(Calendar.MONTH);
            case "year":
                return cal.get(Calendar.YEAR);
            default:
                return 0;
        }
    }

    public static int dateItem(String item, String date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(date));
        switch (item){
            case "day":
                return cal.get(Calendar.DAY_OF_MONTH);
            case "month":
                return cal.get(Calendar.MONTH);
            case "year":
                return cal.get(Calendar.YEAR);
            default:
                return 0;
        }
    }

    public static String todayString(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH) + "/" +( cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
    }

    public static Date todayDate() throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy").parse(Misc.todayString());
    }

    public static Date anyDate(String date) throws ParseException {
        return new Date(date);
    }

    public static String anyDateString(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
    }

    public static int differenceInMonths(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        int diff = 0;
        if (c2.after(c1)) {
            while (c2.after(c1)) {
                c1.add(Calendar.MONTH, 1);
                if (c2.after(c1)) {
                    diff++;
                }
            }
        } else if (c2.before(c1)) {
            while (c2.before(c1)) {
                c1.add(Calendar.MONTH, -1);
                if (c1.before(c2)) {
                    diff--;
                }
            }
        }
        return diff;
    }

    public static Long calculatePendingDue(JSONArray arrRents, JSONArray arrRentRates) throws JSONException, ParseException {
        Long aRent = Long.parseLong("0");
        Long pRent = Long.parseLong("0");
        for (int i = 1; i < arrRentRates.length(); i++) {
            aRent = aRent + Long.parseLong(arrRentRates.getJSONObject(i-1).getString("total_rent_agreed")) * differenceInMonths(Misc.anyDate(arrRentRates.getJSONObject(i-1).getString("rent_effective_from")), Misc.anyDate(arrRentRates.getJSONObject(i).getString("rent_effective_from")));
        }
        if (Misc.anyDate(arrRentRates.getJSONObject(arrRentRates.length()-1).getString("rent_effective_from")).before(Misc.todayDate())){
            aRent = aRent + Long.parseLong(arrRentRates.getJSONObject(arrRentRates.length()-1).getString("total_rent_agreed")) * differenceInMonths(Misc.anyDate(arrRentRates.getJSONObject(arrRentRates.length()-1).getString("rent_effective_from")), Misc.todayDate());
        }
        for (int i = 0; i < arrRents.length(); i++) {
            pRent = pRent + Long.parseLong(arrRents.getJSONObject(i).getString("amount_paid"));
        }
        return aRent - pRent;
    }

    public static String getMAC(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
//        macAddress = wInfo.getMacAddress("eth0");
//        macAddress = wInfo.getIpAddress(true);
//        macAddress = wInfo.getIpAddress(false);
        return macAddress.toString();
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static String getAndroidID(Context context){
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static void saveToExternalFolder(String jsonFile, Context context){
        File myExternalFile;
        String filepath = "MyFileStorage";
        String filename = "dataset.txt";
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {

        }else {
            myExternalFile = new File(context.getExternalFilesDir(filepath), filename);
            try {
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(jsonFile.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFromExternalFolder(Context context){
        File myExternalFile;
        String filepath = "MyFileStorage";
        String filename = "dataset.txt";
        String myData ="";
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {

        }else {
            myExternalFile = new File(context.getExternalFilesDir(filepath), filename);
            try {
                FileInputStream fis = new FileInputStream(myExternalFile);
                DataInputStream in = new DataInputStream(fis);
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    myData = myData + strLine;
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return myData;
        }
        return "";
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {

            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                //Read byte from input stream

                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

}

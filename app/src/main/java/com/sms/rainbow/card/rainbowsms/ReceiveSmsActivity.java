package com.sms.rainbow.card.rainbowsms;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiveSmsActivity extends Activity implements OnItemClickListener {

    private static ReceiveSmsActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static ReceiveSmsActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();

        ActivityCompat.requestPermissions(ReceiveSmsActivity.this,
                new String[]{Manifest.permission.READ_SMS},
                1);
        ActivityCompat.requestPermissions(ReceiveSmsActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);
       // new SendMessagesONline().execute("Hello World");
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_sms);
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

       // new SendMessagesONline().execute("datax");
     //   new ResponseServer().execute("data am coming");
        if (ContextCompat.checkSelfPermission(ReceiveSmsActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ReceiveSmsActivity.this,
                    Manifest.permission.READ_SMS)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(ReceiveSmsActivity.this,
                        new String[]{Manifest.permission.READ_SMS},
                        1);
                ActivityCompat.requestPermissions(ReceiveSmsActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        1);
                refreshSmsInbox();
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    public void refreshSmsInbox() {
        // Here, thisActivity is the current activity

        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        long timeMillis = smsInboxCursor.getColumnIndex("date");
        Date date = new Date(timeMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        String dateText = format.format(date);

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            String str = smsInboxCursor.getString(indexAddress) +" at "+
                    "\n" + smsInboxCursor.getString(indexBody) +dateText+ "\n";
            String address = "AirtelMoney";
            String datacheck = "recieved";
            String datacheck2 = "Balance";
            String datacheck3 = "Trans";

           // List<Character> names = new ArrayList<>();

            if(smsInboxCursor.getString(indexAddress).equals(address)){
                //transmit the name
                arrayAdapter.add(smsInboxCursor.getString(indexBody));

                new ResponseServer().execute(smsInboxCursor.getString(indexBody).toString());

                                }
        } while (smsInboxCursor.moveToNext());

    }



    public class ResponseServer extends AsyncTask<String, Void, String> {



        @Override
        protected String doInBackground(String... params) {

            // These two need to be declared outside the try/catch

            String response = null;
           // Log.d(TAG, params[0]);
           // Log.d(TAG, params[1]);

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                URL url = new URL("http://ec2-54-191-230-33.us-west-2.compute.amazonaws.com/rainbow/view/readingserver.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

               // Intent intent = getIntent();
                Log.d("datasent", params[0]);
               // String email = intent.getStringExtra("email");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("data",params[0]);
                //.appendQueryParameter("card_buyer", "2016");
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                // InputStream is = conn.getInputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                // Toast.makeText(PayActivity.this,"Payment Done...", Toast.LENGTH_SHORT).show();


                // Read the input stream into a String
                InputStream inputStream = conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                BufferedReader  reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                response = buffer.toString();
                Log.d("Response", response);
                // return response;

                // Toast.makeText(PayActivity.this,response, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                //  Log.e( e.toString());
               // Toast.makeText(PayActivity.this,"Failed Payment", Toast.LENGTH_SHORT).show();
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

           // Toast.makeText(PayActivity.this,s, Toast.LENGTH_SHORT).show();
        }
    }



    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToCompose(View view) {
        Intent intent = new Intent(ReceiveSmsActivity.this, SendSmsActivity.class);
        startActivity(intent);
    }
}
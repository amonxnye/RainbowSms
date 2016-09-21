package com.sms.rainbow.card.rainbowsms;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ResponseServer().execute("received");
    }

    public void goToInbox(View view) {
        Intent intent = new Intent(MainActivity.this, ReceiveSmsActivity.class);
        startActivity(intent);
    }

    public void goToCompose(View view) {
        Intent intent = new Intent(MainActivity.this, SendSmsActivity.class);
        startActivity(intent);
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
                        .appendQueryParameter("data", "received");
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
    }
}

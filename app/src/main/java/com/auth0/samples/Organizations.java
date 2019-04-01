package com.auth0.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;
import static com.auth0.samples.SignIn.EXTRA_ID_TOKEN;

public class Organizations extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    TextView headerText;
    private static final String API_URL = "https://rollcall-api.herokuapp.com/api/user/get/";
    private static String name = "";
    private static String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizations);
        headerText = (TextView) findViewById(R.id.header);
        recyclerView = (RecyclerView) findViewById(R.id.myOrgs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        Button qr = (Button) findViewById(R.id.signedIn);
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextActivity(QRScanner.class);
            }
        });


        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        final String email = getIntent().getStringExtra("USER_EMAIL");
        final String name = getIntent().getStringExtra("USER_NAME");

                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .header("Authorization", "Bearer " + accessToken)
                                    .get()
                                    .url(API_URL + email)
                                    .build();
                            Log.d("URL", API_URL);
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Request request, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        throw new IOException("Unexpected code " + response);
                                    } else {
                                        try {
                                            String jsonData = response.body().string();
                                            Log.d("qweqw,", jsonData);
                                            JSONObject reader = new JSONObject(jsonData);
                                            JSONArray Jarray = reader.getJSONArray("enrollments");
                                            final String Orgs[] = new String[Jarray.length()];
                                            for (int i=0; i<Jarray.length(); i++){
                                                JSONObject object = Jarray.getJSONObject(i);
                                                Orgs[i] = object.getString("organization");
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recyclerView.setAdapter(new Adapter(Organizations.this, Orgs));
                                                }
                                            });
                                        } catch (JSONException a) {
                                            a.printStackTrace();
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                headerText.setText(name + "'s Organizations");
                                            }
                                        });

                                    }

                                }
                            });

    }
    private void showNextActivity(Class next) {
        Intent intent = new Intent(Organizations.this, next);
        intent.putExtra(EXTRA_ACCESS_TOKEN, getIntent().getStringExtra(EXTRA_ACCESS_TOKEN));
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_NAME",name);
        startActivity(intent);
        finish();
    }
}
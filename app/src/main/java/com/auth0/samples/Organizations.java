package com.auth0.samples;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;
import static com.auth0.samples.SignIn.EXTRA_ID_TOKEN;

public class Organizations extends Activity {

    TextView headerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizations);
        headerText = (TextView) findViewById(R.id.header);

        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        String idToken = getIntent().getStringExtra(EXTRA_ID_TOKEN);
        final String name = getIntent().getStringExtra("USERS_NAME");
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .header("Authorization", "Bearer " + accessToken)
                                    .get()
                                    .url("https://rollcall-api.herokuapp.com/api/user/get/nihirpatel@ufl.edu")
                                    .build();
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
                                            JSONObject reader = new JSONObject(jsonData);
                                            JSONObject enrollments = reader.getJSONObject("enrollments");
                                            final String org = reader.getString("organization");
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


}
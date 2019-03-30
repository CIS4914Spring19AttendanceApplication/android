package com.auth0.samples;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private String lets;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizations);
        headerText = (TextView) findViewById(R.id.header);
        recyclerView = (RecyclerView) findViewById(R.id.myOrgs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        final String email = getIntent().getStringExtra("USER_EMAIL");
        //final String name = getIntent().getStringExtra("USER_NAME");

                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .header("Authorization", "Bearer " + accessToken)
                                    .get()
                                    .url(API_URL + "nihirpatel@ufl.edu")
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
                                            JSONObject reader = new JSONObject(jsonData);
                                            JSONArray Jarray = reader.getJSONArray("enrollments");
                                            Log.d("asdfas", jsonData);
                                            Log.d("asdasdf", Jarray.toString());
                                            final String Orgs[] = new String[Jarray.length()];
                                            for (int i=0; i<Jarray.length(); i++){
                                                JSONObject object = Jarray.getJSONObject(i);
                                                Orgs[i] = object.getString("organization");
                                            }
                                            Log.d("qsadg", Orgs[1]);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recyclerView.setAdapter(new Adapter(Organizations.this, Orgs));
                                                }
                                            });
                                            name = reader.getString("first_name");
                                        } catch (JSONException a) {
                                            a.printStackTrace();
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                headerText.setText(name + "'s Organizations");
                                                //recyclerView.setAdapter(new Adapter(this, Orgs));

                                            }
                                        });

                                    }

                                }
                            });

    }
}
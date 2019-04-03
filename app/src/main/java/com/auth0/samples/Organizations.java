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

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;

public class Organizations extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    TextView headerText;
    private static final String API_URL = "https://rollcall-api.herokuapp.com/api/user/history/";
    private static String name = "";
    private static String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizations);
        headerText = (TextView) findViewById(R.id.header);

        Button qr = (Button) findViewById(R.id.signedIn);
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextActivity();
            }
        });


        //Gets Access Token, Username, and Email to use for API GET function
        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        final String email = getIntent().getStringExtra("USER_EMAIL");
        final String name = getIntent().getStringExtra("USER_NAME");


        //Sets up new OKhttp client for HTTP GET Request
        OkHttpClient client = new OkHttpClient();

        //Builds the request body
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .url(API_URL + "seboli@ufl.edu")
                .build();

        //Implements the API Call asynchronously
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
                        //Set the variable jsonData to be the response's body
                        String jsonData = response.body().string();

                        //Output for debugging purposes
                        Log.d("qweqw,", jsonData);

                        //Set Array list for RecylcerView
                        final ArrayList<ExampleItem> usersOrgs = new ArrayList<>();

                        //Set a new JSONObject reader to read the json data from the response
                        JSONArray orgArray = new JSONArray(jsonData);

                        //Gets all the user's orgs
                        for(int a = 0; a < orgArray.length(); a++) {

                            JSONObject orgObject = orgArray.getJSONObject(a);
                            final String orgName = orgObject.getString("org");

                            Log.d("Here", orgName);

                            //Sets up variable to give org point summary and users' current points
                            int totalOrgPoints = 0;
                            int currentUserPoints = 0;

                            //Gets all the user's events
                            JSONArray eventArray = orgObject.getJSONArray("events");
                            for (int i = 0; i < eventArray.length(); i++) {
                                JSONObject eventObject = eventArray.getJSONObject(i);

                                String eventName = eventObject.getString("name");
                                String eventDate = eventObject.getString("date");
                                String eventLocation = eventObject.getString("location");

                                Log.d("There", eventName + " " + eventDate + " " + eventLocation);

                                //Gets all the events's points
                                JSONArray pointArray = eventObject.getJSONArray("point_categories");
                                for (int j = 0; j < pointArray.length(); j++) {
                                    JSONObject pointObject = pointArray.getJSONObject(j);

                                    int points = pointObject.getInt("points");
                                    String pointName = pointObject.getString("name");

                                    Log.d("Inside", points + " " + pointName);

                                }

                            }


                            //Gets all the user's point statuses
                            JSONArray pointStatusArray = orgObject.getJSONArray("point_status");
                            for (int k = 0; k < pointStatusArray.length(); k++) {
                                JSONObject pointStatusObject = pointStatusArray.getJSONObject(k);

                                String category = pointStatusObject.getString("category");
                                int totalPoints = pointStatusObject.getInt("total_points");
                                int currentPoints = pointStatusObject.getInt("current_points");

                                currentUserPoints = currentPoints + currentUserPoints;
                                totalOrgPoints = totalPoints + totalOrgPoints;
                            }

                            final String pointSummary = Integer.toString(currentUserPoints) + "/" + Integer.toString(totalOrgPoints);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usersOrgs.add(new ExampleItem(orgName, pointSummary));

                                    mRecyclerView = findViewById(R.id.recyclerView);
                                    mRecyclerView.setHasFixedSize(true);

                                    mLayoutManager = new LinearLayoutManager(Organizations.this);
                                    mAdapter = new Adapter(usersOrgs);

                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    mRecyclerView.setAdapter(mAdapter);
                                }
                            });
                        }

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
    private void showNextActivity() {
        Intent intent = new Intent(Organizations.this, QRScanner.class);
        intent.putExtra(EXTRA_ACCESS_TOKEN, getIntent().getStringExtra(EXTRA_ACCESS_TOKEN));
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_NAME",name);
        startActivity(intent);
        finish();
    }
}
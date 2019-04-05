package com.auth0.samples;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.vision.L;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;

public class Organizations extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView mRecyclerView1;
    private RecyclerView.Adapter mAdapter1;
    private RecyclerView.LayoutManager mLayoutManager1;

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

                            final ArrayList<ExamplePoint> usersPoints = new ArrayList<>();
                            final ArrayList<ExampleEvent> usersEvents = new ArrayList<>();

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
                                String eventLocation = eventObject.getString("location");

//                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
//                                Date eventDate = dateFormat.parse(eventObject.getString("date"));
//                                String formattedDate = dateFormat.format(eventObject.getString("date"));

//                                Log.d("There", eventName + " " + formattedDate + " " + eventLocation);

                                //Gets all the events's points
                                JSONArray pointArray = eventObject.getJSONArray("point_categories");
                                String description = "";
                                for (int j = 0; j < pointArray.length(); j++) {
                                    JSONObject pointObject = pointArray.getJSONObject(j);

                                    int points = pointObject.getInt("points");
                                    String pointName = pointObject.getString("name");

                                    Log.d("Inside", points + " " + pointName);
                                    description = description + eventLocation + ", " + Integer.toString(points) + " " + pointName + " point(s)\n";

                                }


                                usersEvents.add( new ExampleEvent(eventName, description));

                            }


                            //Gets all the user's point statuses
                            JSONArray pointStatusArray = orgObject.getJSONArray("point_status");
                            for (int k = 0; k < pointStatusArray.length()-1; k++) {
                                JSONObject pointStatusObject = pointStatusArray.getJSONObject(k);

                                String category = pointStatusObject.getString("category");
                                int totalPoints = pointStatusObject.getInt("total_points");
                                int currentPoints = pointStatusObject.getInt("current_points");
                                boolean complete = false;

                                currentUserPoints = currentPoints + currentUserPoints;
                                totalOrgPoints = totalPoints + totalOrgPoints;

                                if( currentPoints == totalPoints)
                                    complete = true;

                                usersPoints.add(new ExamplePoint(category,currentPoints + "/" + totalPoints, complete));


                            }

                            final boolean orgComplete;

                            if(currentUserPoints == totalOrgPoints)
                                orgComplete = true;
                            else
                                orgComplete = false;

                            final String pointSummary = Integer.toString(currentUserPoints) + "/" + Integer.toString(totalOrgPoints);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TableLayout tableLayout = findViewById(R.id.tableLayout);
                                    Typeface regular = ResourcesCompat.getFont(Organizations.this,R.font.roboto_condensed_regular);

                                    TableRow tr1 = new TableRow(Organizations.this);
                                    TextView orgHeader = new TextView(Organizations.this);
                                    TextView pointHeader = new TextView(Organizations.this);
                                    orgHeader.setText(orgName);
                                    pointHeader.setText(pointSummary);

                                    orgHeader.setTextColor(Color.BLACK);
                                    orgHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
                                    orgHeader.setTypeface(regular);


                                    if(orgComplete)
                                        pointHeader.setTextColor(Color.GREEN);
                                    else
                                        pointHeader.setTextColor(Color.RED);


                                    pointHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
                                    pointHeader.setTypeface(regular);


                                    tr1.addView(orgHeader);
                                    tr1.addView(pointHeader);

                                    TableRow tr2 = new TableRow(Organizations.this);
                                    TableRow tr3 = new TableRow(Organizations.this);

                                    TextView eventHeader = new TextView(Organizations.this);
                                    TextView pointsHeader = new TextView(Organizations.this);

                                    eventHeader.setText(getResources().getString(R.string.Events));
                                    eventHeader.setTextColor(getResources().getColor(R.color.holo_orange_light));
                                    eventHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                                    eventHeader.setTypeface(regular);

                                    pointsHeader.setText(getResources().getString(R.string.Points));
                                    pointsHeader.setTextColor(getResources().getColor(R.color.holo_orange_light));
                                    pointsHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
                                    pointsHeader.setTypeface(regular);

                                    tr2.addView(eventHeader);

                                    tableLayout.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    tableLayout.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                                    for(int c=0; c < usersEvents.size(); c++){
                                        TableRow tr4 = new TableRow(Organizations.this);

                                        TextView eventName = new TextView(Organizations.this);
                                        TextView eventDescription = new TextView(Organizations.this);

                                        eventName.setText(usersEvents.get(c).getEventName() + usersEvents.get(c).getEventDescription());
                                        eventDescription.setText(usersEvents.get(c).getEventDescription());


                                        eventName.setTextColor(getResources().getColor(R.color.black));
                                        eventName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        eventName.setTypeface(regular);

                                        eventDescription.setTextColor(getResources().getColor(R.color.black));
                                        eventDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        eventDescription.setTypeface(regular);


                                        tr4.addView(eventName);
                                        //tr4.addView(eventDescription);


                                        tableLayout.addView(tr4, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                                    }
                                    tr3.addView(pointsHeader);
                                    tableLayout.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));


                                    for(int i=0; i < usersPoints.size(); i++){
                                        TableRow tr6 = new TableRow(Organizations.this);

                                        TextView pointCategory = new TextView(Organizations.this);
                                        TextView pointTotals = new TextView(Organizations.this);

                                        pointCategory.setText(usersPoints.get(i).getPointCategory());
                                        pointTotals.setText(usersPoints.get(i).getCategoryTotalPoints());


                                        pointCategory.setTextColor(getResources().getColor(R.color.black));
                                        pointCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        pointCategory.setTypeface(regular);


                                        if(usersPoints.get(i).getIsComplete())
                                            pointTotals.setTextColor(getResources().getColor(R.color.green));
                                        else
                                            pointTotals.setTextColor(getResources().getColor(R.color.red));

                                        pointTotals.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        pointTotals.setTypeface(regular);

                                        tr6.addView(pointCategory);
                                        tr6.addView(pointTotals);

                                        tableLayout.addView(tr6, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                                    }


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
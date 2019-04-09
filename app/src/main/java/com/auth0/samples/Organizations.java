package com.auth0.samples;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


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

    TextView headerText;
    private static final String API_URL = "https://rollcall-api.herokuapp.com/api/user/history/";
    private static String name = "";
    private static String email = "";
    private int index = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizations);
        headerText = (TextView) findViewById(R.id.header);
        final TableLayout tableLayout = findViewById(R.id.tableLayout);


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
                .url(API_URL + email)
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

                            //Sets up variable to give org point summary and users' current points
                            int totalOrgPoints = 0;
                            int currentUserPoints = 0;

                            //Gets all the user's events
                            JSONArray eventArray = orgObject.getJSONArray("events");
                            for (int i = 0; i < eventArray.length(); i++) {
                                JSONObject eventObject = eventArray.getJSONObject(i);

                                String eventName = eventObject.getString("name");
                                String eventLocation = eventObject.getString("location");

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                Date eventDate = dateFormat.parse(eventObject.getString("date"));

                                dateFormat = new SimpleDateFormat("MM/dd");
                                String formattedDate = dateFormat.format(eventDate);


                                //Gets all the events's points
                                JSONArray pointArray = eventObject.getJSONArray("point_categories");
                                String description = formattedDate + ", " + eventLocation;
                                for (int j = 0; j < pointArray.length(); j++) {
                                    JSONObject pointObject = pointArray.getJSONObject(j);

                                    int points = pointObject.getInt("points");
                                    String pointName = pointObject.getString("name");

                                    description = description + ", " + Integer.toString(points) + " " + pointName + " point(s)";

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

                                    TableRow.LayoutParams text1 = new TableRow.LayoutParams();
                                    text1.width=0;
                                    text1.weight= (float)0.8;
                                    orgHeader.setLayoutParams(text1);

                                    TableRow.LayoutParams text2 = new TableRow.LayoutParams();
                                    text2.width=0;
                                    text2.weight= (float)0.2;

                                    pointHeader.setLayoutParams(text2);

                                    tr1.addView(orgHeader);
                                    tr1.addView(pointHeader);

                                    final TableRow tr2 = new TableRow(Organizations.this);
                                    final TableRow tr3 = new TableRow(Organizations.this);


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
                                    tr2.setVisibility(View.GONE);
                                    tr2.setId(index);

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



                                        TableRow.LayoutParams text3 = new TableRow.LayoutParams();
                                        text3.width=0;
                                        text3.weight= (float)0.7;
                                        eventName.setLayoutParams(text3);
                                        tr4.addView(eventName);
                                        tr4.setVisibility(View.GONE);
                                        tr4.setTag(index);

                                        tableLayout.addView(tr4, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT));
                                        index++;

                                    }
                                    tr3.addView(pointsHeader);
                                    tr3.setVisibility(View.GONE);
                                    tr3.setId(index);

                                    tableLayout.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                                    for(int i=0; i < usersPoints.size(); i++) {
                                        final TableRow tr6 = new TableRow(Organizations.this);

                                        TextView pointCategory = new TextView(Organizations.this);
                                        TextView pointTotals = new TextView(Organizations.this);

                                        pointCategory.setText(usersPoints.get(i).getPointCategory());
                                        pointTotals.setText(usersPoints.get(i).getCategoryTotalPoints());


                                        pointCategory.setTextColor(getResources().getColor(R.color.black));
                                        pointCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        pointCategory.setTypeface(regular);


                                        if (usersPoints.get(i).getIsComplete())
                                            pointTotals.setTextColor(getResources().getColor(R.color.green));
                                        else
                                            pointTotals.setTextColor(getResources().getColor(R.color.red));

                                        pointTotals.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                                        pointTotals.setTypeface(regular);


                                        TableRow.LayoutParams text4 = new TableRow.LayoutParams();
                                        text4.width = 0;
                                        text4.weight = (float) 0.85;
                                        pointCategory.setLayoutParams(text4);

                                        TableRow.LayoutParams text5 = new TableRow.LayoutParams();
                                        text5.width = 0;
                                        text5.weight = (float) 0.15;
                                        pointTotals.setLayoutParams(text5);


                                        tr6.addView(pointCategory);
                                        tr6.addView(pointTotals);
                                        tr6.setVisibility(View.GONE);
                                        tr6.setTag(index);

                                        tableLayout.addView(tr6, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                        index++;
                                    }

                                    tr1.setOnClickListener(new View.OnClickListener(){
                                        @Override
                                        public void onClick(View v) {
                                            if(tr2.getVisibility() == View.GONE) {
                                                tr2.setVisibility(View.VISIBLE);
                                                tr3.setVisibility(View.VISIBLE);

                                                for(int i = tr2.getId(); i < usersEvents.size()+tr2.getId(); i++)
                                                    tableLayout.findViewWithTag(i).setVisibility(View.VISIBLE);

                                                for(int i = tr3.getId(); i < usersPoints.size()+tr3.getId(); i++)
                                                    tableLayout.findViewWithTag(i).setVisibility(View.VISIBLE);
                                            }
                                            else{
                                                tr2.setVisibility(View.GONE);
                                                tr3.setVisibility(View.GONE);

                                                for(int i = tr2.getId(); i < usersEvents.size()+tr2.getId(); i++)
                                                    tableLayout.findViewWithTag(i).setVisibility(View.GONE);

                                                for(int i = tr3.getId(); i < usersPoints.size()+tr3.getId(); i++)
                                                    tableLayout.findViewWithTag(i).setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException a) {
                        a.printStackTrace();
                    }catch (ParseException d){
                        d.printStackTrace();
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
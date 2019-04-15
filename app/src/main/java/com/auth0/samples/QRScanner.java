package com.auth0.samples;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.SurfaceView;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.refactor.lib.colordialog.PromptDialog;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Callback;

import com.google.android.gms.location.FusedLocationProviderClient;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;


public class QRScanner extends Activity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    TextView welcomeText;
    private static final String API_URL_GET = "https://rollcall-api.herokuapp.com/api/user/onboardcheck/";
    private static final String API_URL_CHECKIN = "https://rollcall-api.herokuapp.com/api/event/";
    private static String email = "";
    private static String first_name = "";
    private static String last_name = "";
    private static String phone = "";
    private static String accessToken = "";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_ACCESS_REQUEST_CODE = 100;
    private static JSONArray qrResult;
    private static SparseArray<Barcode> qrCodes;
    private static String latitude;
    private static String longitude;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scanner);
        welcomeText = (TextView) findViewById(R.id.welcome);
        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        textView = (TextView) findViewById(R.id.description);
        Button orgs = (Button) findViewById(R.id.organizations);

        orgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextActivity(Organizations.class);
            }
        });
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(QRScanner.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        requestPermission();

        accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);


        //Getting User's email to store and send to next activity
        Auth0 auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);
        auth0.setLoggingEnabled(true);
        AuthenticationAPIClient authenticationAPIClient = new AuthenticationAPIClient(auth0);
        authenticationAPIClient.userInfo(accessToken)
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile userinfo) {
                        email = userinfo.getEmail();
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .header("Authorization", "Bearer " + accessToken)
                                .get()
                                .url(API_URL_GET + email)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    Log.d("Unexpected code", response.body().toString());
                                } else {
                                    try {
                                        String jsonData = response.body().string();
                                        JSONObject reader = new JSONObject(jsonData);
                                        first_name = reader.getString("first_name").replace(" ", "");
                                        last_name = reader.getString("last_name").replace(" ", "");
                                        phone = reader.getString("phone").replace(" ", "");
                                    } catch (JSONException a) {
                                        a.printStackTrace();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            welcomeText.setText("Welcome, " + first_name + "!");
                                        }
                                    });

                                }

                            }
                        });
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                    }
                });

        barcodeDetector = new BarcodeDetector.Builder(QRScanner.this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(QRScanner.this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch(IOException e)
                {
                    e.printStackTrace();
                }
            }



            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections ) {
                qrCodes = detections.getDetectedItems();

                if (qrCodes.size() != 0) {
                    try {
                        qrResult = new JSONArray(qrCodes.valueAt(0).displayValue);
                        checkIn(qrResult);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cameraSource.stop();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    private void checkIn(JSONArray use) {
        try {
            JSONObject obj = use.getJSONObject(0);
            String type = obj.getString("type");
            Log.d("qwerqwr", obj.toString());
            String orgId = obj.getString("org_id");


            if (type.equals("org")) {
                final String org_name = obj.getString("org_name");

                JSONObject checkIn = new JSONObject();

                checkIn.put("org_id", orgId);
                checkIn.put("email", email);

                OkHttpClient client1 = new OkHttpClient();

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, checkIn.toString());

                Request request1 = new Request.Builder()
                        .header("Authorization", "Bearer " + accessToken)
                        .url(API_URL_CHECKIN + "addBoard/")
                        .post(requestBody)
                        .build();

                client1.newCall(request1).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new PromptDialog(QRScanner.this)
                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                            .setAnimationEnable(true)
                                            .setTitleText("Uh Oh")
                                            .setContentText("You are already a board member.")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                        Log.d("did not work", "permissions");
                                                    }
                                                    try {
                                                        cameraSource.start(surfaceView.getHolder());
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new PromptDialog(QRScanner.this)
                                            .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                                            .setAnimationEnable(true)
                                            .setTitleText("Success")
                                            .setContentText("You are now a board member of " + org_name + ". Thanks for joining!")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                        Log.d("did not work", "permissions");
                                                    }
                                                    try {
                                                        cameraSource.start(surfaceView.getHolder());
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            });
                        }
                    }
                });

            } else if (type.equals("event")) {
                String eventId = obj.getString("event_id");
                final String event_name = obj.getString("event_name");
                final String location_enforce = obj.getString("location_enforce");
                final JSONArray points = obj.getJSONArray("point_categories");
                final JSONArray additionalFields = obj.getJSONArray("additional_fields");

                if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission();
                }

//                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                longitude = Double.toString(location.getLatitude());
//                latitude = Double.toString(location.getLatitude());

                client = LocationServices.getFusedLocationProviderClient(this);
                client.getLastLocation().addOnSuccessListener(QRScanner.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!= null){
                            latitude = Double.toString(location.getLatitude());
                            longitude = Double.toString(location.getLongitude());

                        }
                    }
                });
                

                final JSONObject checkIn = new JSONObject();

                checkIn.put("email", email);
                checkIn.put("first_name", first_name);
                checkIn.put("last_name", last_name);
                checkIn.put("phone", phone);

                checkIn.put("event_id", eventId);
                checkIn.put("org_id", orgId);


                checkIn.put("point_categories", points);

                if (location_enforce.equals("true")) {
                    checkIn.put("latitude", latitude);
                    checkIn.put("longitude", longitude);
                }


                Log.d("qwerqwe1", checkIn.toString());

                if (additionalFields.length() == 0) {
                    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, checkIn.toString());

                    Request request2 = new Request.Builder()
                            .header("Authorization", "Bearer " + accessToken)
                            .url(API_URL_CHECKIN + "checkin/")
                            .post(requestBody)
                            .build();
                    OkHttpClient client1 = new OkHttpClient();

                    client1.newCall(request2).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                try {
                                    String jsonMessage = response.body().string();
                                    JSONObject responseType = new JSONObject(jsonMessage);
                                    String message = responseType.getString("message");

                                    Log.d("wqeqwr", message);

                                    if (message.equals("You are not in the proper location to sign in to this event.")) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new PromptDialog(QRScanner.this)
                                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                                        .setAnimationEnable(true)
                                                        .setTitleText("Uh Oh")
                                                        .setContentText("You are not in the proper location to sign in to this event.")
                                                        .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                            @Override
                                                            public void onClick(PromptDialog dialog) {
                                                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                    Log.d("did not work", "permissions");
                                                                }
                                                                try {
                                                                    cameraSource.start(surfaceView.getHolder());
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                            }
                                        });
                                    } else if (message.equals("You have already signed in to this event.")) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new PromptDialog(QRScanner.this)
                                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                                        .setAnimationEnable(true)
                                                        .setTitleText("Uh Oh")
                                                        .setContentText("You have already signed in to this event.")
                                                        .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                            @Override
                                                            public void onClick(PromptDialog dialog) {
                                                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                    Log.d("did not work", "permissions");
                                                                }
                                                                try {
                                                                    cameraSource.start(surfaceView.getHolder());
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                            }
                                        });
                                    } else {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                PromptDialog alert = new PromptDialog(QRScanner.this);
                                                alert.setDialogType(PromptDialog.DIALOG_TYPE_WRONG);
                                                alert.setAnimationEnable(true);
                                                alert.setTitleText("Uh Oh");
                                                alert.setContentText("This event is not open for sign in right now.");
                                                alert.setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                    @Override
                                                    public void onClick(PromptDialog dialog) {
                                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                            Log.d("did not work", "permissions");
                                                        }
                                                        try {
                                                            cameraSource.start(surfaceView.getHolder());
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException a) {
                                    a.printStackTrace();
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new PromptDialog(QRScanner.this)
                                                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                                                .setAnimationEnable(true)
                                                .setTitleText("Success")
                                                .setContentText("You have signed in to " + event_name + ". Thanks for coming!")
                                                .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                    @Override
                                                    public void onClick(PromptDialog dialog) {
                                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                            Log.d("did not work", "permissions");
                                                        }
                                                        try {
                                                            cameraSource.start(surfaceView.getHolder());
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    //First must check if there are Additional Fields/Questions for the Event
                    if (additionalFields.length() != 0) {
                        final String[] questions = new String[additionalFields.length()];
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final PromptDialog additional = new PromptDialog(QRScanner.this, R.style.AlertDialog)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                                        .setAnimationEnable(true)
                                        .setTitleText("Additional Questions")
                                        .setContentText("Please provide the following information");

                                additional.setCanceledOnTouchOutside(false);
                                additional.setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                    @Override
                                    public void onClick(PromptDialog dialog) {

                                        final AlertDialog.Builder builder = new AlertDialog.Builder(QRScanner.this);
                                        final LinearLayout layout = new LinearLayout(QRScanner.this);
                                        layout.setOrientation(LinearLayout.VERTICAL);

                                        for (int index = 0; index < additionalFields.length(); index++) {
                                            try {
                                                JSONObject additionalQuestion = additionalFields.getJSONObject(index);
                                                questions[index] = additionalQuestion.getString("question");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            Log.d("weqwer123", questions[index]);
                                            final EditText input = new EditText(QRScanner.this);
                                            input.setHint(questions[index]);
                                            input.setGravity(Gravity.CENTER_HORIZONTAL);
                                            input.setTag(index);

                                            layout.addView(input, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        }

                                        layout.setGravity(View.TEXT_ALIGNMENT_CENTER);
                                        builder.setView(layout);
                                        builder.setTitle("Additional Questions")
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            for (int index = 0; index < additionalFields.length(); index++) {
                                                                JSONObject userResponse = additionalFields.getJSONObject(index);
                                                                EditText userInput = layout.findViewWithTag(index);
                                                                String result = userInput.getText().toString();
                                                                userResponse.put("response", result);
                                                                Log.d("qwerqwrqqw1212", result);
                                                                additionalFields.put(index, userResponse);
                                                            }
                                                            checkIn.put("additional_fields", additionalFields);
                                                            Log.d("aasdasdas", additionalFields.get(0).toString());
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                                        RequestBody requestBody = RequestBody.create(JSON, checkIn.toString());

                                                        Request request2 = new Request.Builder()
                                                                .header("Authorization", "Bearer " + accessToken)
                                                                .url(API_URL_CHECKIN + "checkin/")
                                                                .post(requestBody)
                                                                .build();
                                                        OkHttpClient client1 = new OkHttpClient();

                                                        client1.newCall(request2).enqueue(new Callback() {
                                                            @Override
                                                            public void onFailure(Call call, IOException e) {
                                                                e.printStackTrace();
                                                            }

                                                            @Override
                                                            public void onResponse(Call call, Response response) throws IOException {
                                                                if (!response.isSuccessful()) {
                                                                    try {
                                                                        String jsonMessage = response.body().string();
                                                                        JSONObject responseType = new JSONObject(jsonMessage);
                                                                        String message = responseType.getString("message");

                                                                        Log.d("wqeqwr", message);

                                                                        if (message.equals("You are not in the proper location to sign in to this event.")) {

                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    new PromptDialog(QRScanner.this)
                                                                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                                                                            .setAnimationEnable(true)
                                                                                            .setTitleText("Uh Oh")
                                                                                            .setContentText("You are not in the proper location to sign in to this event.")
                                                                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                                                                @Override
                                                                                                public void onClick(PromptDialog dialog) {
                                                                                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                                                        Log.d("did not work", "permissions");
                                                                                                    }
                                                                                                    try {
                                                                                                        cameraSource.start(surfaceView.getHolder());
                                                                                                    } catch (IOException e) {
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            }).show();
                                                                                }
                                                                            });
                                                                        } else if (message.equals("You have already signed in to this event.")) {

                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    new PromptDialog(QRScanner.this)
                                                                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                                                                            .setAnimationEnable(true)
                                                                                            .setTitleText("Uh Oh")
                                                                                            .setContentText("You have already signed in to this event.")
                                                                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                                                                @Override
                                                                                                public void onClick(PromptDialog dialog) {
                                                                                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                                                        Log.d("did not work", "permissions");
                                                                                                    }
                                                                                                    try {
                                                                                                        cameraSource.start(surfaceView.getHolder());
                                                                                                    } catch (IOException e) {
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                    dialog.dismiss();
                                                                                                }
                                                                                            }).show();
                                                                                }
                                                                            });
                                                                        } else {

                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    PromptDialog alert = new PromptDialog(QRScanner.this);
                                                                                    alert.setDialogType(PromptDialog.DIALOG_TYPE_WRONG);
                                                                                    alert.setAnimationEnable(true);
                                                                                    alert.setTitleText("Uh Oh");
                                                                                    alert.setContentText("This event is not open for sign in right now.");
                                                                                    alert.setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                                                        @Override
                                                                                        public void onClick(PromptDialog dialog) {
                                                                                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                                                Log.d("did not work", "permissions");
                                                                                            }
                                                                                            try {
                                                                                                cameraSource.start(surfaceView.getHolder());
                                                                                            } catch (IOException e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            dialog.dismiss();
                                                                                        }
                                                                                    });
                                                                                    alert.show();
                                                                                }
                                                                            });
                                                                        }
                                                                    } catch (JSONException a) {
                                                                        a.printStackTrace();
                                                                    }
                                                                }
                                                                else {
                                                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                                        Log.d("did not work", "permissions");
                                                                    }
                                                                    try {
                                                                        cameraSource.start(surfaceView.getHolder());
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                        builder.create();
                                        builder.show();
                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            Log.d("did not work", "permissions");
                                        }
                                        try {
                                            cameraSource.start(surfaceView.getHolder());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                    }
                                }).show();
                            }
                        });
                    }
                }
            }else {
                //When the QR code is not for an event or an Org
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new PromptDialog(QRScanner.this)
                                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                .setAnimationEnable(true)
                                .setTitleText("Uh Oh")
                                .setContentText("Not a valid QR code.")
                                .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                    @Override
                                    public void onClick(PromptDialog dialog) {
                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            Log.d("did not work", "permissions");
                                        }
                                        try {
                                            cameraSource.start(surfaceView.getHolder());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showNextActivity(Class next) {
        Intent intent = new Intent(QRScanner.this, next);
        intent.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_NAME",first_name);
        startActivity(intent);
        finish();
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(QRScanner.this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }
}

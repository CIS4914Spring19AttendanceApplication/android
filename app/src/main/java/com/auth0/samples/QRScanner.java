package com.auth0.samples;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.SurfaceView;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
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

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;


public class QRScanner extends Activity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    TextView welcomeText;
    private static final String API_URL_GET = "https://rollcall-api.herokuapp.com/api/user/onboardcheck/";
    private static final String API_URL_CHECKIN = "https://rollcall-api.herokuapp.com/api/checkin/";
    private static String email = "";
    private static String name = "";
    private static String accessToken = "";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static JSONArray qrResult;

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
            ActivityCompat.requestPermissions(QRScanner.this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        Log.d("HERE", accessToken);



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
                                        name = reader.getString("first_name").replace(" ", "");
                                    } catch (JSONException a) {
                                        a.printStackTrace();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            welcomeText.setText("Welcome, " + name+ "!");
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


        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
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
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections ) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if (qrCodes.size() != 0) {
                    try {
                        qrResult = new JSONArray(qrCodes.valueAt(0).displayValue);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
                                release();
//                            }
//                        });
                        checkIn(qrResult);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void checkIn(JSONArray use){
        try {

            JSONObject obj = use.getJSONObject(0);
            String type = obj.getString("type");
            Log.d("asdfa", type);

            if (type.equals("org")) {
                String id = obj.getString("org_id");
                final String org_name = obj.getString("org_name");


                JsonObject checkIn = new JsonObject();

                checkIn.addProperty("org_id", id);
                checkIn.addProperty("email", email);

                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, obj.toString());

                Request request = new Request.Builder()
                        .header("Authorization", "Bearer " + accessToken)
                        .url(API_URL_CHECKIN)
                        .post(requestBody)
                        .build();
                OkHttpClient client1 = new OkHttpClient();

                client1.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d("wqeqwr", response.body().string());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new PromptDialog(QRScanner.this)
                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                            .setAnimationEnable(true)
                                            .setTitleText("Uh Oh")
                                            .setContentText("You have already signed in to this organization.")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
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
                                            .setContentText("You have signed in to "+ org_name +". Thanks for joining!")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            });
                        }
                    }
                });

            } else if (type.equals("event")) {
                String id = obj.getString("event_id");
                final String event_name = obj.getString("name");

                JsonObject checkIn = new JsonObject();

                checkIn.addProperty("org_id", id);
                checkIn.addProperty("email", email);
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, obj.toString());

                Request request = new Request.Builder()
                        .header("Authorization", "Bearer " + accessToken)
                        .url(API_URL_CHECKIN)
                        .post(requestBody)
                        .build();
                OkHttpClient client1 = new OkHttpClient();

                client1.newCall(request).enqueue(new Callback() {
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
                                            .setContentText("This event is not open for sign in right now.")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
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
                                            .setContentText("You have signed in to "+ event_name +". Thanks for coming!")
                                            .setPositiveListener("Done", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            });
                        }
                    }
                });
            } else {
                Log.d("Neither org or event", type);
            }
        }  catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void showNextActivity(Class next) {
        Intent intent = new Intent(QRScanner.this, next);
        intent.putExtra(EXTRA_ACCESS_TOKEN, getIntent().getStringExtra(EXTRA_ACCESS_TOKEN));
        intent.putExtra("USER_EMAIL", email);
        intent.putExtra("USER_NAME",name);
        startActivity(intent);
        finish();
    }
}

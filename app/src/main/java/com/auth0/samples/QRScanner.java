package com.auth0.samples;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.SurfaceView;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;
import static com.auth0.samples.SignIn.EXTRA_ID_TOKEN;

public class QRScanner extends Activity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    TextView welcomeText;
    private static final String API_URL = "https://rollcall-api.herokuapp.com/api/user/onboardcheck/";
    private static String email = "";
    private static String name = "";

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

        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        String idToken = getIntent().getStringExtra(EXTRA_ID_TOKEN);

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
                                .url(API_URL + email)
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
                                               name = reader.getString("first_name");
                                           } catch (JSONException a) {
                                               a.printStackTrace();
                                           }
                                           runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   welcomeText.setText("Welcome, " + name + "!");
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
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(this,barcodeDetector)
                .setRequestedPreviewSize(640,480).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try
                {
                    cameraSource.start(holder);
                }catch(IOException e)
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

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();

                if(qrCodes.size() != 0)
                {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            textView.setText(qrCodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

    }

    private void showNextActivity(Class next) {
        Intent intent = new Intent(QRScanner.this, next);
        intent.putExtra(EXTRA_ACCESS_TOKEN, getIntent().getStringExtra(EXTRA_ACCESS_TOKEN));
        intent.putExtra(EXTRA_ID_TOKEN, getIntent().getStringExtra(EXTRA_ID_TOKEN));
        intent.putExtra("USER_EMAIL",email);
        startActivity(intent);
        finish();
    }
}

package com.auth0.samples;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.SurfaceView;

import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
import com.android.volley.RequestQueue;
//import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends Activity {

    SurfaceView surfaceView;
    CameraSource cameraSource;
    TextView textView;
    BarcodeDetector barcodeDetector;
    private TextView welcomeText;
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private static final String TAG = UserProfile.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        welcomeText = (TextView)findViewById(R.id.welcome);
        textView = (TextView)findViewById(R.id.description);


        OkHttpClient client = new OkHttpClient();

        String url = "https://github.com/rollcall-app/api";
        String authHeader = getIntent().getStringExtra("login_token");

        Request request = new Request.Builder()
                .addHeader("authorization", authHeader)
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    UserProfile.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            welcomeText.setText(myResponse);
                        }
                    });
                }
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

}

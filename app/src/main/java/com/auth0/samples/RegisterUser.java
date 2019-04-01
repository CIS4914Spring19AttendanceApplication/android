package com.auth0.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.auth0.samples.SignIn.EXTRA_ACCESS_TOKEN;

public class RegisterUser extends Activity {

    TextView hello;
    EditText first_name;
    EditText last_name;
    EditText phone;
    EditText userEmail;
    private static String email = "";
    private static String firstName = "";
    private static String lastName = "";
    private static String phoneNumber = "";
    private static String year = "";
    private static final String API_URL_REGISTER = "https://rollcall-api.herokuapp.com/api/user/registeruser/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);

        final String accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        email = getIntent().getStringExtra("USER_EMAIL");

        hello = (TextView) findViewById(R.id.hello);
        first_name = (EditText)findViewById(R.id.editFirstName);
        last_name = (EditText)findViewById(R.id.editLastName);
        phone = (EditText)findViewById(R.id.editPhone);

        userEmail = (EditText)findViewById(R.id.editEmail);
        userEmail.setText(email);
        userEmail.setEnabled(false);

        final NumberPicker np = (NumberPicker) findViewById(R.id.yearPicker);
        np.setMinValue(0);
        np.setMaxValue(4);
        np.setDisplayedValues(new String[] {"Freshman", "Sophmore", "Junior", "Senior", "Graduate"});
        np.setWrapSelectorWheel(false);


        Button register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               firstName = first_name.getText().toString();
               lastName = last_name.getText().toString();
               phoneNumber = phone.getText().toString();
               if(np.getValue() == 0)
                   year = "Freshman";
               if(np.getValue() == 1)
                   year = "Sophmore";
               if(np.getValue() == 2)
                   year = "Junior";
               if(np.getValue() == 3)
                   year = "Senior";


                JSONArray array = new JSONArray();

                    JsonObject obj = new JsonObject();
                        obj.addProperty("email", email);
                        obj.addProperty("first_name", firstName);
                        obj.addProperty("last_name", lastName);
                        obj.addProperty("phone", phoneNumber);
                        obj.addProperty("year", year);
                    array.put(obj);

                OkHttpClient client = new OkHttpClient();


                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON,obj.toString());

                Request request = new Request.Builder()
                        .header("Authorization", "Bearer " + accessToken)
                        .url(API_URL_REGISTER)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected code " + response.body().string());
                        }
                        else {
                            Headers responseHeaders = response.headers();
                            for(int i = 0; i < responseHeaders.size(); i++) {
                                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                            }
                        }
                    }
                });

                showNextActivity();
            }
        });

    }
    private void showNextActivity() {
        Intent intent = new Intent(RegisterUser.this, QRScanner.class);
        intent.putExtra(EXTRA_ACCESS_TOKEN, getIntent().getStringExtra(EXTRA_ACCESS_TOKEN));
        intent.putExtra("USER_EMAIL",email);
        startActivity(intent);
        finish();
    }
}

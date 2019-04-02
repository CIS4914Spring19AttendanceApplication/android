package com.auth0.samples;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.authentication.storage.CredentialsManagerException;
import com.auth0.android.authentication.storage.SecureCredentialsManager;
import com.auth0.android.authentication.storage.SharedPreferencesStorage;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SignIn extends Activity {

    private TextView token;
    private Auth0 auth0;
    private SecureCredentialsManager credentialsManager;
    private static final String API_URL = "https://rollcall-api.herokuapp.com/api/user/onboardcheck/";
    private static String email = "";


    @SuppressWarnings("unused")
    private static final int CODE_DEVICE_AUTHENTICATION = 22;
    public static final String KEY_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS";
    public static final String EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN";
    public static final String EXTRA_ID_TOKEN = "com.auth0.ID_TOKEN";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView rollCall = (TextView)findViewById(R.id.rollCall);

        //Setup Credentials Manager
        auth0 = new Auth0(this);
        auth0.setLoggingEnabled(true);
        auth0.setOIDCConformant(true);
        credentialsManager = new SecureCredentialsManager(this, new AuthenticationAPIClient(auth0), new SharedPreferencesStorage(this));

        //Require device authentication before obtaining credentials
        credentialsManager.requireAuthentication(this, CODE_DEVICE_AUTHENTICATION, getString(R.string.request_credentials_title), null);

        //Check if this was launched after a logout
        if (getIntent().getBooleanExtra(KEY_CLEAR_CREDENTIALS, true)) {
            credentialsManager.clearCredentials();
        }

        //Check if a log in button must be shown
        if (!credentialsManager.hasValidCredentials()) {
            setContentView(R.layout.signin);
            token = (TextView) findViewById(R.id.token);
            Button signIn = (Button) findViewById(R.id.SignIn);
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });
            return;
        }

        //Obtain the existing credentials and move to the next activity
        credentialsManager.getCredentials(new BaseCallback<Credentials, CredentialsManagerException>() {
            @Override
            public void onSuccess(final Credentials credentials) {
                showNextActivity(credentials, QRScanner.class);
            }

            @Override
            public void onFailure(CredentialsManagerException error) {
                //Authentication cancelled by the user. Exit the app
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (credentialsManager.checkAuthenticationResult(requestCode, resultCode)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showNextActivity(Credentials credentials, Class next) {
        Intent intent = new Intent(SignIn.this, next);
        intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.getAccessToken());
        intent.putExtra(EXTRA_ID_TOKEN, credentials.getIdToken());
        intent.putExtra("USER_EMAIL",email);
        startActivity(intent);
        finish();
    }


    private void login() {
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withAudience("https://rollcall-api.herokuapp.com")
                .withScope("openid profile email read:current_user update:current_user_metadata")
                .start(this, webCallback);
    }

    private final AuthCallback webCallback = new AuthCallback() {
        @Override
        public void onFailure(@NonNull final Dialog dialog) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }

        @Override
        public void onFailure(AuthenticationException exception) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignIn.this, "Log In - Error Occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onSuccess(@NonNull final Credentials credentials) {
            credentialsManager.saveCredentials(credentials);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SignIn.this, "Log In - Success", Toast.LENGTH_SHORT).show();
                    AuthenticationAPIClient authenticationAPIClient = new AuthenticationAPIClient(auth0);
                    authenticationAPIClient.userInfo(credentials.getAccessToken())
                            .start(new BaseCallback<UserProfile, AuthenticationException>() {
                                @Override
                                public void onSuccess(UserProfile userinfo) {
                                    email = userinfo.getEmail();
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .header("Authorization", "Bearer " + credentials.getAccessToken())
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
                                                showNextActivity(credentials, RegisterUser.class);
                                            } else {
                                                showNextActivity(credentials, QRScanner.class);
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(AuthenticationException error) {

                                }
                            });
                }
            });
            //showNextActivity(credentials);
        }
    };
}


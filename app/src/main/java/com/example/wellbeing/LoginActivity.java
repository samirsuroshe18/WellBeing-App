package com.example.wellbeing;

import com.example.wellbeing.UtilsServices.ApiClient;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    TextView mov_to_signUp, forgot_password;
    EditText email_editText, pass_editText;
    Button sign_in_btn;
    String email, password, accessToken, refreshToken, resMsg;
    SharedPreferenceClass sharedPreference;
    ProgressDialog progressDialog;
    ProgressBar login_progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your account");
        mov_to_signUp = findViewById(R.id.mov_to_signUp);
        forgot_password = findViewById(R.id.forgot_password);
        email_editText = findViewById(R.id.email_editText);
        pass_editText = findViewById(R.id.pass_editText);
        sign_in_btn = findViewById(R.id.sign_in_btn);
        login_progress = findViewById(R.id.login_progress);
        sharedPreference = new SharedPreferenceClass(LoginActivity.this);

        mov_to_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });

       sign_in_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               email = email_editText.getText().toString().trim();
               password = pass_editText.getText().toString();
               if(!email.isEmpty() && !password.isEmpty()){
                   LoginUser(view);
               }else {
                   Toast.makeText(LoginActivity.this, "All fields are required!!", Toast.LENGTH_SHORT).show();
               }
           }
       });
    }

    private void LoginUser(View view) {
        sign_in_btn.setEnabled(false);
        sign_in_btn.setText("");
        login_progress.setVisibility(View.VISIBLE);

        final HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        String apiKey = ApiClient.BASE_URL + "/users/login";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONObject dataObject = response.getJSONObject("data");
                        accessToken = dataObject.getString("accessToken");
                        refreshToken = dataObject.getString("refreshToken");
                        sharedPreference.setValue_string("accessToken", accessToken);
                        sharedPreference.setValue_string("refreshToken", refreshToken);
                        sharedPreference.setUser(dataObject.getJSONObject("loggedInUser").getString("_id"));
                        resMsg = response.getString("message");
                        Toast.makeText(LoginActivity.this, resMsg, Toast.LENGTH_SHORT).show();
                        login_progress.setVisibility(View.GONE);
                        sign_in_btn.setText("Login");
                        sign_in_btn.setEnabled(true);
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(LoginActivity.this, "No accessToken found in the response", Toast.LENGTH_SHORT).show();
                        login_progress.setVisibility(View.GONE);
                        sign_in_btn.setText("Login");
                        sign_in_btn.setEnabled(true);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    login_progress.setVisibility(View.GONE);
                    sign_in_btn.setText("Login");
                    sign_in_btn.setEnabled(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ApiClient.showError(LoginActivity.this, error);
                error.printStackTrace();
                login_progress.setVisibility(View.GONE);
                sign_in_btn.setText("Login");
                sign_in_btn.setEnabled(true);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = ApiClient.getQueue(LoginActivity.this);
        requestQueue.add(jsonObjectRequest);

    }
}
package com.example.wellbeing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    TextView mov_to_signUp;
    EditText email_editText, pass_editText;
    Button sign_in_btn;
    String email, password, accessToken, refreshToken, resMsg;
    SharedPreferenceClass sharedPreference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your account");
        mov_to_signUp = findViewById(R.id.mov_to_signUp);
        email_editText = findViewById(R.id.email_editText);
        pass_editText = findViewById(R.id.pass_editText);
        sign_in_btn = findViewById(R.id.sign_in_btn);
        sharedPreference = new SharedPreferenceClass(LoginActivity.this);

        mov_to_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

       sign_in_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               email = email_editText.getText().toString();
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
        progressDialog.show();

        final HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/login";

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
                        resMsg = response.getString("message");
                        Log.d("Response : ", accessToken);
                        Toast.makeText(LoginActivity.this, resMsg, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                        progressDialog.dismiss();
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(LoginActivity.this, "No accessToken found in the response", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                        progressDialog.dismiss();
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        progressDialog.dismiss();
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                            progressDialog.dismiss();
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                            progressDialog.dismiss();
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                            progressDialog.dismiss();
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                            progressDialog.dismiss();
                            progressDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                    progressDialog.dismiss();
                }
                Log.i("Error", errorMessage);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                progressDialog.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }
}
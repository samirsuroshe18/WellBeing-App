package com.example.wellbeing;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.ParseHtmlClass;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextView mov_to_signUp;
    EditText email_editText, pass_editText;
    Button sign_in_btn;
    String email, password, accessToken, refreshToken, resMsg;
    SharedPreferenceClass sharedPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        final HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        String apiKey = "https://wellbeing-backend-blush.vercel.app/api/v1/users/login";

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
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(LoginActivity.this, "No accessToken found in the response", Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                try {
                    JSONObject errRes = new JSONObject(errMsg);
                    String err = errRes.getString("error");
                    Toast.makeText(LoginActivity.this, err, Toast.LENGTH_SHORT).show();
                    Log.d("Error Message : ", errMsg );
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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
    }
}
package com.example.wellbeing;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextView mov_to_logIn;
    EditText email_editText, name_editText, pass_editText;
    Button sign_up_btn;
    String name, email, password, accessToken, refreshToken, resMsg;
    SharedPreferenceClass sharedPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mov_to_logIn = findViewById(R.id.mov_to_logIn);
        email_editText = findViewById(R.id.email_editText);
        name_editText = findViewById(R.id.name_editText);
        pass_editText = findViewById(R.id.pass_editText);
        sign_up_btn = findViewById(R.id.sign_in_btn);
        sharedPreference = new SharedPreferenceClass(RegisterActivity.this);

        mov_to_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = name_editText.getText().toString();
                email = email_editText.getText().toString();
                password = pass_editText.getText().toString();
                if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
                    registerUser(view);
                }else {
                    Toast.makeText(RegisterActivity.this, "All fields are required!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void registerUser(View view) {

        final HashMap<String, String> params = new HashMap<>();
        params.put("userName", name);
        params.put("email", email);
        params.put("password", password);

        String apiKey = "https://wellbeing-azhs.onrender.com/api/v1/users/register";

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
                        Toast.makeText(RegisterActivity.this, resMsg, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(RegisterActivity.this, "No accessToken found in the response", Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                new ParseHtmlClass(error, RegisterActivity.this);
                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                try {
                    JSONObject errRes = new JSONObject(errMsg);
                    String err = errRes.getString("error");
                    Toast.makeText(RegisterActivity.this, err, Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        requestQueue.add(jsonObjectRequest);
    }
}
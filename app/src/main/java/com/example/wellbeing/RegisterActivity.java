package com.example.wellbeing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.FileUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.UtilsServices.UriToByteArrayConverter;
import com.example.wellbeing.UtilsServices.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private static final int gallery_pic_id = 100;
    int MY_SOCKET_TIMEOUT_MS = 10000; // 10 seconds
    TextView mov_to_logIn;
    EditText email_editText, name_editText, pass_editText;
    Button sign_up_btn;
    String name, email, password, accessToken, refreshToken, resMsg, fileName, fileType;
    SharedPreferenceClass sharedPreference;
    ProgressDialog progressDialog;
    ImageView plusIconIv;
    CircleImageView circleImageView;
    byte[] multiMediaByteArray;
    Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Register");
        progressDialog.setMessage("Registering to your account");
        mov_to_logIn = findViewById(R.id.mov_to_logIn);
        email_editText = findViewById(R.id.email_editText);
        name_editText = findViewById(R.id.name_editText);
        pass_editText = findViewById(R.id.pass_editText);
        sign_up_btn = findViewById(R.id.sign_in_btn);
        plusIconIv = findViewById(R.id.plusIconIV);
        circleImageView = findViewById(R.id.circleImageView);
        sharedPreference = new SharedPreferenceClass(RegisterActivity.this);

        plusIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && android.os.ext.SdkExtensions.getExtensionVersion(android.os.Build.VERSION_CODES.R) >= 2) {
                    Intent gallery_intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                    startActivityForResult(gallery_intent, gallery_pic_id);
                }
            }
        });

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
                if(selectedImageUri == null){
                    Toast.makeText(RegisterActivity.this, "Please select your profile", Toast.LENGTH_SHORT).show();
                } else if (name.isEmpty() && email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!!", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(view);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery_pic_id){
            assert data != null;
            selectedImageUri = data.getData();
            if (selectedImageUri!=null){
                fileName = FileUtils.getFileName(this, selectedImageUri);
                fileType = FileUtils.getMimeType(this, selectedImageUri);
                try {
                    multiMediaByteArray = UriToByteArrayConverter.convertUriToByteArray(RegisterActivity.this, selectedImageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                circleImageView.setImageURI(data.getData());
            }
        }
    }


    private void registerUser(View view) {
        progressDialog.show();
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/register";

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        JSONObject result = null;
                        try {
                            result = new JSONObject(resultResponse);
                            String status = result.getString("statusCode");
                            String message = result.getString("message");
                            if (status.equals("200")) {
                                // tell everybody you have succeed upload image and post strings
                                accessToken = result.getJSONObject("data").getString("accessToken");
                                refreshToken = result.getJSONObject("data").getString("refreshToken");
                                sharedPreference.setValue_string("accessToken", accessToken);
                                sharedPreference.setValue_string("refreshToken", refreshToken);
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                finish();
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                Log.i("Unexpected", message);
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                            String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            try {
                                JSONObject errRes = new JSONObject(errMsg);
                                String err = errRes.getString("error");
                                Toast.makeText(RegisterActivity.this, err, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        } else {
                            // Handle the case when the error or its networkResponse is null
                            Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                }) {
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                params.put("profilePicture", new DataPart(fileName, multiMediaByteArray, fileType));
                return params;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> text = new HashMap<>();
                text.put("userName", name);
                text.put("email", email);
                text.put("password", password);
                return text;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
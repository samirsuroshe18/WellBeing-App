package com.example.wellbeing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.FileUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.UtilsServices.UriToByteArrayConverter;
import com.example.wellbeing.UtilsServices.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private static final int gallery_pic_id = 100;
    private static final int gallery_vid_id = 200;
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    byte[] multiMediaByteArray;
    EditText descriptionET;
    ImageView image, send, imageUpload, videoUpload;
    VideoView video;
    String task, description, multiMedia, mediaType, accessToken, fileName, fileType;
    SharedPreferenceClass sharedPreferenceClass;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        image = findViewById(R.id.image);
        descriptionET = findViewById(R.id.description_et);
        video = findViewById(R.id.video);
        imageUpload = findViewById(R.id.image_upload);
        videoUpload = findViewById(R.id.video_upload);
        send = findViewById(R.id.upload_post);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        task = sharedPreferenceClass.getValue_string("taskId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Upload Post");
        progressDialog.setMessage("Post is uploading");

        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && android.os.ext.SdkExtensions.getExtensionVersion(android.os.Build.VERSION_CODES.R) >= 2) {
                    Intent gallery_intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                    startActivityForResult(gallery_intent, gallery_pic_id);
                }
            }
        });

        videoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, gallery_vid_id);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                description = descriptionET.getText().toString();
                uploadPost();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery_pic_id){
            fileName = FileUtils.getFileName(this, data.getData());
            fileType = FileUtils.getMimeType(this, data.getData());
            Toast.makeText(this, fileType, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();
            try {
                multiMediaByteArray = UriToByteArrayConverter.convertUriToByteArray(PostActivity.this, data.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            if you want to see the size of the file
            long sizeInBytes = multiMediaByteArray.length;
            double sizeInKB = sizeInBytes / 1024.0;
            double sizeInMB = sizeInKB / 1024.0;
            video.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
//            Bitmap gallery_photo = (Bitmap) data.getExtras().get("data");
            image.setImageURI(data.getData());
            mediaType = "image";
        }

        if (requestCode==gallery_vid_id){
            fileName = FileUtils.getFileName(this, data.getData());
            fileType = FileUtils.getMimeType(this, data.getData());
            Toast.makeText(this, fileType, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();
            try {
                multiMediaByteArray = UriToByteArrayConverter.convertUriToByteArray(PostActivity.this, data.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaType = "video";
            video.setVisibility(View.VISIBLE);
            image.setVisibility(View.INVISIBLE);
            video.setVideoURI(data.getData());
            video.start();
        }
    }

    public void uploadPost(){
        progressDialog.show();
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/upload/upload-post";
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = null;
                        try {
                            resultResponse = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                            Log.d("Response : ", resultResponse);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            JSONObject result = new JSONObject(resultResponse);
                            String status = result.getString("status");
                            String message = result.getString("message");

                            if (status.equals("200")) {
                                // tell everybody you have succeed upload image and post strings
                                Toast.makeText(PostActivity.this, message, Toast.LENGTH_SHORT).show();
                                sharedPreferenceClass.setValue_string("statusFlag", "completed");
                                startActivity(new Intent(PostActivity.this, HomeActivity.class));
                                finish();
                                progressDialog.dismiss();
                            } else {
                                Log.i("Unexpected", message);
                                progressDialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
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
                            Toast.makeText(PostActivity.this, result, Toast.LENGTH_SHORT).show();
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
                                    errorMessage = message+" Please login again";
                                    progressDialog.dismiss();
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message+ " Check your inputs";
                                    progressDialog.dismiss();
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message+" Something is getting wrong";
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                            }
                        }
                        Log.i("Error", errorMessage);
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                })
        {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("multiMedia", new DataPart(fileName, multiMediaByteArray, fileType));
                return params;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> text = new HashMap<>();
                text.put("task", task);
                text.put("description", description);
                text.put("mediaType", mediaType);
                return text;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }
}
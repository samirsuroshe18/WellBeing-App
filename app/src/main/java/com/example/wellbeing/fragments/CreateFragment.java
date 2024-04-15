package com.example.wellbeing.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.R;
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
import java.util.Objects;

public class CreateFragment extends Fragment {
    private static final int gallery_pic_id = 100;
    private static final int gallery_vid_id = 200;
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    Uri imgUri, videoUri;
    byte[] multiMediaByteArray;
    EditText taskTitle, taskDescription, taskTime;
    ImageView image, send, imageUpload, videoUpload;
    VideoView video;
    String title, description, time, mediaType, accessToken, fileName;
    SharedPreferenceClass sharedPreferenceClass;
    ProgressDialog progressDialog;
    public CreateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        image = view.findViewById(R.id.image);
        video = view.findViewById(R.id.video);
        imageUpload = view.findViewById(R.id.image_upload);
        videoUpload = view.findViewById(R.id.video_upload);
        send = view.findViewById(R.id.upload_post);
        taskTitle = view.findViewById(R.id.taskTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        taskTime = view.findViewById(R.id.taskTime);
        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Create Task");
        progressDialog.setMessage("Task is uploading");

        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    // For Android 11 and above, use the new storage access framework
                    Intent gallery_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    gallery_intent.addCategory(Intent.CATEGORY_OPENABLE);
                    gallery_intent.setType("image/*");
                    startActivityForResult(gallery_intent, gallery_pic_id);
                } else {
                    // For Android 9 and below, use the older ACTION_PICK intent
                    Intent gallery_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery_intent, gallery_pic_id);
                }
            }
        });

        videoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    // For Android 11 and above, use the new storage access framework
                    Intent gallery_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    gallery_intent.addCategory(Intent.CATEGORY_OPENABLE);
                    gallery_intent.setType("video/*");
                    startActivityForResult(gallery_intent, gallery_vid_id);
                } else {
                    // For Android 10 and below, use the older ACTION_PICK intent
                    Intent gallery_intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    gallery_intent.setType("video/*");
                    startActivityForResult(gallery_intent, gallery_vid_id);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = taskTitle.getText().toString();
                description = taskDescription.getText().toString();
                time = taskTime.getText().toString();
                if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (imgUri == null && videoUri == null) {
                    Toast.makeText(getContext(), "Please provide media reference", Toast.LENGTH_SHORT).show();
                } else {
                    uploadTask();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery_pic_id){
            if (resultCode == RESULT_OK){
                imgUri = data.getData();
                if (imgUri != null){
                    try {
                        fileName = FileUtils.getFileName(getContext(), imgUri);
                        multiMediaByteArray = UriToByteArrayConverter.convertUriToByteArray(requireContext(), imgUri);
                        video.setVisibility(View.INVISIBLE);
                        image.setVisibility(View.VISIBLE);
                        image.setImageURI(imgUri);
                        mediaType = "image";
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
//            if you want to see the size of the file
                    long sizeInBytes = multiMediaByteArray.length;
                    double sizeInKB = sizeInBytes / 1024.0;
                    double sizeInMB = sizeInKB / 1024.0;

//            Bitmap gallery_photo = (Bitmap) data.getExtras().get("data");

                }else {
                    Toast.makeText(getContext(), "Please Select an image", Toast.LENGTH_SHORT).show();
                }
            }
        }

        if (requestCode==gallery_vid_id){
            if (resultCode == RESULT_OK){
                videoUri = data.getData();
                if (videoUri!=null){
                    fileName = FileUtils.getFileName(getContext(), videoUri);
                    try {
                        multiMediaByteArray = UriToByteArrayConverter.convertUriToByteArray(requireContext(), videoUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mediaType = "video";
                    video.setVisibility(View.VISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    video.setVideoURI(data.getData());
                    video.start();
                }else {
                    Toast.makeText(getContext(), "Please Select an image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void uploadTask(){
        progressDialog.show();
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/tasklist/create-task";
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data, StandardCharsets.UTF_8);
                        try {
                            JSONObject result = new JSONObject(resultResponse);
                            String status = result.getString("status");
                            String message = result.getString("message");

                            if (status.equals("200")) {
                                // tell everybody you have succeed upload image and post strings
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                taskTitle.setText("");
                                taskDescription.setText("");
                                taskTime.setText("");
                                image.setVisibility(View.INVISIBLE);
                                video.setVisibility(View.INVISIBLE);
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
                            String result = new String(networkResponse.data, StandardCharsets.UTF_8);
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
                params.put("taskReference", new DataPart(fileName, multiMediaByteArray));
                return params;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> text = new HashMap<>();
                text.put("title", title);
                text.put("description", description);
                text.put("timeToComplete", time);
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
        Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));

        volleyMultipartRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }
}
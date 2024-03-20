package com.example.wellbeing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.wellbeing.UtilsServices.HideKeyboardClass;
import com.example.wellbeing.adapters.CommentAdapter;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.models.CommentModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    ArrayList<CommentModel> commentList;
    CommentAdapter adapter;
    RecyclerView commentRecyclerView;
    String multiMedia, accessToken, content;
    EditText commentInput;
    ImageView sendCommentBtn, backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentInput = findViewById(R.id.comment_et);
        sendCommentBtn = findViewById(R.id.send_comment_btn);
        backBtn = findViewById(R.id.back_arrow);

        Intent intent = getIntent();
        multiMedia = intent.getStringExtra("_id");
        accessToken = intent.getStringExtra("accessToken");

        commentList = new ArrayList<>();
        commentRecyclerView = findViewById(R.id.comment_recycler_view);

        getComments();

        adapter = new CommentAdapter(commentList, this);
        commentRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(layoutManager);

        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = commentInput.getText().toString();
                sendComment();
                commentInput.setText("");
                new HideKeyboardClass(view, CommentActivity.this);

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void getComments(){
            String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/comment/get-comment";

            final HashMap<String, String> params = new HashMap<>();
            params.put("multiMedia", multiMedia);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response != null) {
                            JSONArray dataObject = response.getJSONArray("data");
                            for (int i = 0; i<dataObject.length(); i++){
                                commentList.add(new CommentModel(dataObject.getJSONObject(i).getString("_id"),
                                        dataObject.getJSONObject(i).getJSONObject("commentedBy").getString("userName"),
                                        dataObject.getJSONObject(i).getJSONObject("commentedBy").getString("profilePicture"),
                                        dataObject.getJSONObject(i).getString("content"),
                                        dataObject.getJSONObject(i).getString("createdAt")
                                        ));
                            }
                            adapter.notifyDataSetChanged();

                        } else {
                            // Handle the case where "accessToken" key is not present in the JSON response
                            Toast.makeText(CommentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
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
                        } else if (error.getClass().equals(NoConnectionError.class)) {
                            errorMessage = "Failed to connect server";
                        }
                    } else {
                        String result = null;
                        try {
                            result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                            Log.d("Error : ", result);
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(CommentActivity.this, result, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject response = new JSONObject(result);
                            String status = response.getString("status");
                            String message = response.getString("message");

                            Log.e("Error Status", status);
                            Log.e("Error Message", message);

                            if (networkResponse.statusCode == 404) {
                                errorMessage = "Resource not found";
                            } else if (networkResponse.statusCode == 401) {
                                errorMessage = message+" Unauthorized";
                            } else if (networkResponse.statusCode == 400) {
                                errorMessage = message+ "Bad request";
                            } else if (networkResponse.statusCode == 500) {
                                errorMessage = message+" Something is getting wrong";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("Error", errorMessage);
                    Toast.makeText(CommentActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    error.printStackTrace();

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer "+accessToken);
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(CommentActivity.this);
            requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
        }

    public void sendComment(){
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/comment/post-comment";

        final HashMap<String, String> params = new HashMap<>();
        params.put("multiMedia", multiMedia);
        params.put("content", content);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");
                        for (int i = 0; i<dataObject.length(); i++){
                            commentList.add(new CommentModel(dataObject.getJSONObject(i).getString("_id"),
                                    dataObject.getJSONObject(i).getJSONObject("commentedBy").getString("userName"),
                                    dataObject.getJSONObject(i).getJSONObject("commentedBy").getString("profilePicture"),
                                    dataObject.getJSONObject(i).getString("content"),
                                    dataObject.getJSONObject(i).getString("createdAt")
                            ));
                        }
                        adapter.notifyItemInserted(commentList.size()-1);
                        commentRecyclerView.scrollToPosition(commentList.size() - 1);

                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(CommentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    e.printStackTrace();
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
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(CommentActivity.this, result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(CommentActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CommentActivity.this);
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }
}
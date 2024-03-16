package com.example.wellbeing;

import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.HideKeyboardClass;
import com.example.wellbeing.adapters.CommentAdapter;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.models.CommentModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

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
                    if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                        String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        try {
                            JSONObject errRes = new JSONObject(errMsg);
                            String err = errRes.getString("error");
                            Toast.makeText(CommentActivity.this, err, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Handle the case when the error or its networkResponse is null
                        Toast.makeText(CommentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

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
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                    String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    try {
                        JSONObject errRes = new JSONObject(errMsg);
                        String err = errRes.getString("error");
                        Toast.makeText(CommentActivity.this, err, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the case when the error or its networkResponse is null
                    Toast.makeText(CommentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

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
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
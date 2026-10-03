package com.example.wellbeing;

import com.example.wellbeing.UtilsServices.ApiClient;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.example.wellbeing.models.CommentModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    ArrayList<CommentModel> commentList;
    CommentAdapter adapter;
    RecyclerView commentRecyclerView;
    String multiMedia, accessToken, content;
    EditText commentInput;
    MaterialButton backBtn;
    MaterialToolbar toolbar;
    FloatingActionButton sendCommentBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        commentInput = findViewById(R.id.comment_et);
        sendCommentBtn = findViewById(R.id.send_comment_btn);

        Intent intent = getIntent();
        multiMedia = intent.getStringExtra("_id");
        accessToken = intent.getStringExtra("accessToken");

        commentList = new ArrayList<>();
        commentRecyclerView = findViewById(R.id.comment_recycler_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> {
            // Option 1: Use onBackPressed() (traditional way)
//            onBackPressed();

            // Option 2: Or simply finish the activity
            finish();

            // Option 3: Or navigate to specific activity
            // Intent intent = new Intent(AcceptedTaskActivity.this, MainActivity.class);
            // startActivity(intent);
            // finish();
        });
        getComments();

        adapter = new CommentAdapter(commentList, this);
        commentRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentRecyclerView.setLayoutManager(layoutManager);

        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                content = commentInput.getText().toString().trim();
                if (content.isEmpty()) {
                    Toast.makeText(CommentActivity.this, "Write a comment first", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendComment();
                commentInput.setText("");
                new HideKeyboardClass(view, CommentActivity.this);

            }
        });
    }

    public void getComments(){
            String apiKey = ApiClient.BASE_URL + "/comment/get-comment";

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

                    ApiClient.showError(CommentActivity.this, error);
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

            RequestQueue requestQueue = ApiClient.getQueue(CommentActivity.this);
            requestQueue.add(jsonObjectRequest);

        }

    public void sendComment(){
        String apiKey = ApiClient.BASE_URL + "/comment/post-comment";

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

                ApiClient.showError(CommentActivity.this, error);
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

        RequestQueue requestQueue = ApiClient.getQueue(CommentActivity.this);
        requestQueue.add(jsonObjectRequest);

    }
}
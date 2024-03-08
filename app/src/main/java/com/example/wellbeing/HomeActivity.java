package com.example.wellbeing;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.fragments.CreateFragment;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.fragments.LeaderboardFragment;
import com.example.wellbeing.fragments.ProfileFragment;
import com.example.wellbeing.fragments.TaskFragment;
import com.example.wellbeing.models.PostModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    ArrayList<PostModel> postList;
    PostsAdapter adapter;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    HomeFragment homeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNav);
        postList = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        adapter = new PostsAdapter(postList, this, accessToken);

        getPosts();

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
        if (homeFragment == null) {
            homeFragment = new HomeFragment(this, postList, adapter);
        }

        loadFrag(homeFragment, true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id==R.id.navigation_home){
                    if (homeFragment.isVisible()) {
                        // Refresh the data if the HomeFragment is currently visible
                        postList.clear();
                        getUpdatedPosts();
                        loadFrag(new HomeFragment(HomeActivity.this, postList, adapter), false);
                    } else {
                        loadFrag(homeFragment, false);
                    }
                } else if (id==R.id.navigation_task) {
                    loadFrag(new TaskFragment(), false);
                } else if (id==R.id.navigation_create) {
                    loadFrag(new CreateFragment(), false);
                } else if (id==R.id.navigation_leaderboard) {
                    loadFrag(new LeaderboardFragment(), false);
                }else {
                    loadFrag(new ProfileFragment(), false);
                }
                return true;
            }
        });
    }

    public void loadFrag(Fragment fragment, boolean flag){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.add(R.id.container, fragment);
        }else {
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    private void getPosts() {

        String apiKey = "http://192.168.219.221:10000/api/v1/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");
                        for (int i=0; i<dataObject.length(); i++){
                            PostModel postModel = new PostModel();

                            postModel.set_id(dataObject.getJSONObject(i).getString("_id"));
                            postModel.setUserProfile(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("pofilePicture"));
                            postModel.setUserName(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("userName"));
                            postModel.setCreatedAt(dataObject.getJSONObject(i).getString("createdAt"));
                            postModel.setDescription(dataObject.getJSONObject(i).getString("discription"));
                            postModel.setMedia(dataObject.getJSONObject(i).getString("multiMedia"));
                            postModel.setTotalLikes(dataObject.getJSONObject(i).getInt("likes"));
                            postModel.setTotalDislikes(dataObject.getJSONObject(i).getInt("dislikes"));
                            postModel.setTotalComments(dataObject.getJSONObject(i).getInt("comments"));
                            postModel.setMediaType(dataObject.getJSONObject(i).getString("mediaType"));
                            postModel.setTaskId(dataObject.getJSONObject(i).getString("task"));
                            postModel.setDuration(dataObject.getJSONObject(i).getString("duration"));

                            postList.add(postModel);
                            adapter.notifyDataSetChanged();
                        }

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
                        Toast.makeText(HomeActivity.this, err, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the case when the error or its networkResponse is null
                    Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    private void getUpdatedPosts() {

        String apiKey = "http://192.168.219.221:10000/api/v1/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");
                        for (int i=0; i<dataObject.length(); i++){
                            PostModel postModel = new PostModel();

                            postModel.set_id(dataObject.getJSONObject(i).getString("_id"));
                            postModel.setUserProfile(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("pofilePicture"));
                            postModel.setUserName(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("userName"));
                            postModel.setCreatedAt(dataObject.getJSONObject(i).getString("createdAt"));
                            postModel.setDescription(dataObject.getJSONObject(i).getString("discription"));
                            postModel.setMedia(dataObject.getJSONObject(i).getString("multiMedia"));
                            postModel.setTotalLikes(dataObject.getJSONObject(i).getInt("likes"));
                            postModel.setTotalDislikes(dataObject.getJSONObject(i).getInt("dislikes"));
                            postModel.setTotalComments(dataObject.getJSONObject(i).getInt("comments"));
                            postModel.setMediaType(dataObject.getJSONObject(i).getString("mediaType"));
                            postModel.setTaskId(dataObject.getJSONObject(i).getString("task"));
                            postModel.setDuration(dataObject.getJSONObject(i).getString("duration"));

                            postList.add(postModel);
                            adapter.notifyDataSetChanged();
                        }

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
                        Toast.makeText(HomeActivity.this, err, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the case when the error or its networkResponse is null
                    Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

}
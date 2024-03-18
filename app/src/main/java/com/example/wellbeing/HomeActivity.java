package com.example.wellbeing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.ConnectivityUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.fragments.CreateFragment;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.fragments.LeaderboardFragment;
import com.example.wellbeing.fragments.ProfileFragment;
import com.example.wellbeing.fragments.TaskFragment;
import com.example.wellbeing.models.PostModel;
import com.example.wellbeing.models.TaskModel;
import com.example.wellbeing.models.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    String accessToken, fragmentTagName;
    SharedPreferenceClass sharedPreferenceClass;
    ArrayList<PostModel> postList;
    ArrayList<UserModel> userInfo;
    PostsAdapter adapter;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    HomeFragment homeFragment;
    TaskFragment taskFragment;
    CreateFragment createFragment;
    LeaderboardFragment leaderboardFragment;
    ProfileFragment profileFragment;
    CircleImageView userProfileCV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNav2);
        postList = new ArrayList<>();
        userInfo = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        adapter = new PostsAdapter(postList, this, accessToken);
        userProfileCV = findViewById(R.id.userProfileCV);

        userProfileCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profileFragment");
                if ( profileFragment == null){
                    profileFragment = new ProfileFragment();
                    loadFrag(profileFragment, "profileFragment", false);
                }else  {
                    if (profileFragment.isVisible()){
                        profileFragment = new ProfileFragment();
                        loadFrag(profileFragment, "profileFragment", false);
                    }
                    loadFrag(profileFragment, "profileFragment", false);
                }
            }
        });


        if (ConnectivityUtils.isConnectedToInternet(getApplicationContext())) {
            getPosts();
            getUserInfo();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }


        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
        if (homeFragment == null){
            homeFragment = new HomeFragment(postList, adapter);
        }
        loadFrag(homeFragment, "homeFragment", true);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id==R.id.navigation_home){
                    homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
                    if ( homeFragment == null){
                        homeFragment = new HomeFragment(postList, adapter);
                        loadFrag(homeFragment, "homeFragment", false);
                    }else  {
                        if (homeFragment.isVisible()){
                            postList.clear();
                            getUpdatedPosts();
                            homeFragment = new HomeFragment(postList, adapter);
                            loadFrag(homeFragment, "homeFragment", false);
                        }
                        loadFrag(homeFragment, "homeFragment", false);
                    }
                } else if (id==R.id.navigation_task) {
                    taskFragment = (TaskFragment) getSupportFragmentManager().findFragmentByTag("taskFragment");
                    if ( taskFragment == null){
                        taskFragment = new TaskFragment();
                        loadFrag(taskFragment, "taskFragment", false);
                    }else  {
                        if (taskFragment.isVisible()){
                            taskFragment = new TaskFragment();
                            loadFrag(taskFragment, "taskFragment", false);
                        }
                        loadFrag(taskFragment, "taskFragment", false);
                    }
                } else if (id==R.id.navigation_create) {
                    createFragment = (CreateFragment) getSupportFragmentManager().findFragmentByTag("createFragment");
                    if ( createFragment == null){
                        createFragment = new CreateFragment();
                        loadFrag(createFragment, "createFragment", false);
                    }else  {
                        loadFrag(createFragment, "createFragment", false);
                    }
                } else if (id==R.id.navigation_leaderboard) {
                    leaderboardFragment = (LeaderboardFragment) getSupportFragmentManager().findFragmentByTag("leaderboardFragment");
                    if ( leaderboardFragment == null){
                        leaderboardFragment = new LeaderboardFragment();
                        loadFrag(leaderboardFragment, "leaderboardFragment", false);
                    }else  {
                        if (leaderboardFragment.isVisible()){
                            leaderboardFragment = new LeaderboardFragment();
                            loadFrag(leaderboardFragment, "leaderboardFragment", false);
                        }
                        loadFrag(leaderboardFragment, "leaderboardFragment", false);
                    }
                }else {
                    profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profileFragment");
                    if ( profileFragment == null){
                        profileFragment = new ProfileFragment();
                        loadFrag(profileFragment, "profileFragment", false);
                    }else  {
                        if (profileFragment.isVisible()){
                            profileFragment = new ProfileFragment();
                            loadFrag(profileFragment, "profileFragment", false);
                        }
                        loadFrag(profileFragment, "profileFragment", false);
                    }
                }
                return true;
            }
        });

        // This callback is only called when MyFragment is at least started
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.container);
                String currentFragmentTag = currentFragment.getTag();

                // Check if the current fragment is the home fragment
                if (currentFragmentTag != null && currentFragmentTag.equals("homeFragment")) {
                    // Clear the back stack
                    finishAffinity();
                } else {
                    fragmentManager.popBackStack();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        fragmentTagName = intent.getStringExtra("fragmentTagName");
        if (fragmentTagName != null){
            if (fragmentTagName.equals("taskFragment")){
                taskFragment = new TaskFragment();
                loadFrag(taskFragment, "taskFragment", false);
            }
        }

    }

    public void loadFrag(Fragment fragment, String tagName, boolean flag){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.add(R.id.container, fragment, tagName);
        }else {
            fragmentTransaction.replace(R.id.container, fragment, tagName);
        }

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void getPosts() {

        String apiKey = "https://wellbeing-backend-blush.vercel.app/api/v1/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");
                        for (int i=0; i<dataObject.length(); i++){
                            PostModel postModel = new PostModel();

                            postModel.set_id(dataObject.getJSONObject(i).getString("_id"));
                            postModel.setUserProfile(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("profilePicture"));
                            postModel.setUserName(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("userName"));
                            postModel.setCreatedAt(dataObject.getJSONObject(i).getString("createdAt"));
                            postModel.setDescription(dataObject.getJSONObject(i).getString("description"));
                            postModel.setMedia(dataObject.getJSONObject(i).getString("multiMedia"));
                            postModel.setTotalLikes(dataObject.getJSONObject(i).getInt("likes"));
                            postModel.setTotalDislikes(dataObject.getJSONObject(i).getInt("dislikes"));
                            postModel.setTotalComments(dataObject.getJSONObject(i).getInt("comments"));
                            postModel.setMediaType(dataObject.getJSONObject(i).getString("mediaType"));
                            postModel.setTaskId(dataObject.getJSONObject(i).getString("task"));
                            postModel.setDuration(dataObject.getJSONObject(i).getString("duration"));

                            postList.add(postModel);
                            Log.d("getPostData : ", String.valueOf(dataObject));
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
    }

    private void getUpdatedPosts() {

        String apiKey = "https://wellbeing-backend-blush.vercel.app/api/v1/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");

                        for (int i=0; i<dataObject.length(); i++){
                            PostModel postModel = new PostModel();

                            postModel.set_id(dataObject.getJSONObject(i).getString("_id"));
                            postModel.setUserProfile(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("profilePicture"));
                            postModel.setUserName(dataObject.getJSONObject(i).getJSONObject("uploadedBy").getString("userName"));
                            postModel.setCreatedAt(dataObject.getJSONObject(i).getString("createdAt"));
                            postModel.setDescription(dataObject.getJSONObject(i).getString("description"));
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
                        Log.d("getUpdatedPostData : ", String.valueOf(dataObject));
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
    }

    public void getUserInfo(){

        String apiKey = "http://192.168.186.221:10000/api/v1/users/get-userinfo";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {

                        String resMsg = response.getString("message");
                        Toast.makeText(HomeActivity.this, resMsg, Toast.LENGTH_SHORT).show();

                        JSONObject dataObject = (JSONObject) response.getJSONArray("data").get(0);
                        UserModel userModel = new UserModel();

                        userModel.set_id(dataObject.getString("_id"));
                        userModel.setUserName(dataObject.getString("userName"));
                        userModel.setEmail(dataObject.getString("email"));
                        userModel.setProfilePicture(dataObject.getString("profilePicture"));
                        userModel.setTask_completed(String.valueOf(dataObject.getInt("task_completed")));
                        userModel.setCreatedAt(dataObject.getString("createdAt"));
                        userModel.setWellpoints(String.valueOf(dataObject.getJSONObject("wellpoints").getInt("wellpoints")));
                        userModel.setSuccessRate(String.valueOf(dataObject.getInt("successRate")));
                        userModel.setRank(String.valueOf(dataObject.getInt("rank")));

                        userInfo.add(userModel);

                        Picasso.get().load(userInfo.get(userInfo.size()-1).getProfilePicture()).into(userProfileCV);
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
    }

}
package com.example.wellbeing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
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
import com.example.wellbeing.UtilsServices.ConnectivityUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.fragments.CreateFragment;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.fragments.LeaderboardFragment;
import com.example.wellbeing.fragments.ProfileFragment;
import com.example.wellbeing.fragments.TaskFragment;
import com.example.wellbeing.models.PostModel;
import com.example.wellbeing.models.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    //Global declaration
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    BottomNavigationView bottomNavigationView;
    String accessToken, fragmentTagName;
    SharedPreferenceClass sharedPreferenceClass;
    ArrayList<PostModel> postList;
    ArrayList<UserModel> userList;
    PostsAdapter adapter;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FrameLayout container;
    HomeFragment homeFragment;
    TaskFragment taskFragment;
    CreateFragment createFragment;
    LeaderboardFragment leaderboardFragment;
    ProfileFragment profileFragment;
    CircleImageView userProfileCV;
    LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        //Initialization and finding id's of views

        bottomNavigationView = findViewById(R.id.bottomNav2);
        lottieAnimationView = findViewById(R.id.loadingAnim);
        container = findViewById(R.id.container);
        postList = new ArrayList<>();
        userList = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        adapter = new PostsAdapter(postList, this, accessToken);
        userProfileCV = findViewById(R.id.userProfileCV);

        // Home fragment will be shown on home activity as we open our app

        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
        if (homeFragment == null){
            homeFragment = new HomeFragment(postList, adapter);
        }
        loadFrag(homeFragment, "homeFragment", true);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        userProfileCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profileFragment");
                if ( profileFragment == null){
                    profileFragment = new ProfileFragment(container, lottieAnimationView);
                    loadFrag(profileFragment, "profileFragment", false);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                }else  {
                    if (profileFragment.isVisible()){
                        profileFragment = new ProfileFragment(container, lottieAnimationView);
                        loadFrag(profileFragment, "profileFragment", false);
                    }
                    loadFrag(profileFragment, "profileFragment", false);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                }
            }
        });

        // It checks wheather internet connection is on or not

        if (ConnectivityUtils.isConnectedToInternet(getApplicationContext())) {
            getPosts();
            getUserInfo();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

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
                        taskFragment = new TaskFragment(container, lottieAnimationView);
                        loadFrag(taskFragment, "taskFragment", false);
                    }else  {
                        if (taskFragment.isVisible()){
                            taskFragment = new TaskFragment(container, lottieAnimationView);
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
                        leaderboardFragment = new LeaderboardFragment(container, lottieAnimationView);
                        loadFrag(leaderboardFragment, "leaderboardFragment", false);
                    }else  {
                        if (leaderboardFragment.isVisible()){
                            leaderboardFragment = new LeaderboardFragment(container, lottieAnimationView);
                            loadFrag(leaderboardFragment, "leaderboardFragment", false);
                        }
                        loadFrag(leaderboardFragment, "leaderboardFragment", false);
                    }
                }else {
                    profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profileFragment");
                    if ( profileFragment == null){
                        profileFragment = new ProfileFragment(container, lottieAnimationView);
                        loadFrag(profileFragment, "profileFragment", false);
                    }else  {
                        if (profileFragment.isVisible()){
                            profileFragment = new ProfileFragment(container, lottieAnimationView);
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
                assert currentFragment != null;
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
                taskFragment = new TaskFragment(container, lottieAnimationView);
                loadFrag(taskFragment, "taskFragment", false);
                bottomNavigationView.setSelectedItemId(R.id.navigation_task);
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
        lottieAnimationView.setVisibility(View.VISIBLE);
        container.setVisibility(View.INVISIBLE);

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
                            Log.d("getPostData : ", String.valueOf(dataObject));

                        }
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                    container.setVisibility(View.VISIBLE);
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
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(HomeActivity.this, result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                lottieAnimationView.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
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
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }

    private void getUpdatedPosts() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        container.setVisibility(View.INVISIBLE);

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
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                    container.setVisibility(View.VISIBLE);
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
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);

                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(HomeActivity.this, result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                lottieAnimationView.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
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
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }

    public void getUserInfo(){

        container.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);

        String apiKey = "https://wellbeing-backend-blush.vercel.app/api/v1/users/get-userinfo";

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

                        userList.add(userModel);

                        Picasso.get().load(userList.get(userList.size()-1).getProfilePicture()).into(userProfileCV);

                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                    container.setVisibility(View.VISIBLE);
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
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(HomeActivity.this, result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                            container.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                        container.setVisibility(View.VISIBLE);
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                lottieAnimationView.setVisibility(View.INVISIBLE);
                container.setVisibility(View.VISIBLE);
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
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }

}
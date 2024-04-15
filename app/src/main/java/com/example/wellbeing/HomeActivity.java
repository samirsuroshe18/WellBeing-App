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
    ArrayList<UserModel> userList;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FrameLayout container;
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
        container = findViewById(R.id.container);
        userProfileCV = findViewById(R.id.userProfileCV);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        userList = new ArrayList<>();

        // Home fragment will be shown on home activity as we open our app

        if (ConnectivityUtils.isConnectedToInternet(this)) {
            getUserInfo();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
        if (homeFragment == null){
            homeFragment = new HomeFragment();
        }
        loadFrag(homeFragment, "homeFragment", true);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        userProfileCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profileFragment");
                if ( profileFragment == null){
                    profileFragment = new ProfileFragment();
                    loadFrag(profileFragment, "profileFragment", false);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                }else  {
                    if (profileFragment.isVisible()){
                        profileFragment = new ProfileFragment();
                        loadFrag(profileFragment, "profileFragment", false);
                    }
                    loadFrag(profileFragment, "profileFragment", false);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id==R.id.navigation_home){
                    homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");
                    if ( homeFragment == null){
                        homeFragment = new HomeFragment();
                        loadFrag(homeFragment, "homeFragment", false);
                    }else  {
                        if (homeFragment.isVisible()){
                            homeFragment = new HomeFragment();
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

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

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

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        fragmentTagName = intent.getStringExtra("fragmentTagName");
        if (fragmentTagName != null){
            if (fragmentTagName.equals("taskFragment")){
                taskFragment = new TaskFragment();
                loadFrag(taskFragment, "taskFragment", false);
                bottomNavigationView.setSelectedItemId(R.id.navigation_task);
            }
        }

    }


    public void loadFrag(Fragment fragment, String tagName, boolean flag){
        fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tagName);
        if (existingFragment != null && existingFragment.isAdded()) {
            // Fragment already added, no need to add it again
            return;
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.add(R.id.container, fragment, tagName);
        }else {
            fragmentTransaction.replace(R.id.container, fragment, tagName);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }



    public void getUserInfo(){


        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/get-userinfo";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
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
                    Toast.makeText(HomeActivity.this, result, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
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
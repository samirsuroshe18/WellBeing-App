package com.example.wellbeing;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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
import com.example.wellbeing.databinding.ActivityHomeBinding;
import com.example.wellbeing.fragments.CreateFragment;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.fragments.LeaderboardFragment;
import com.example.wellbeing.fragments.TaskFragment;
import com.example.wellbeing.models.UserModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

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
    ActivityHomeBinding binding;
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
    CircleImageView userProfileCV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        userList = new ArrayList<>();

        if (ConnectivityUtils.isConnectedToInternet(this)) {
            getUserInfo();
        } else {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
    }

    public void getUserInfo() {


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

                        Picasso.get().load(userList.get(userList.size() - 1).getProfilePicture()).into(userProfileCV);

                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
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
                            errorMessage = message + " Unauthorized";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + "Bad request";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
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
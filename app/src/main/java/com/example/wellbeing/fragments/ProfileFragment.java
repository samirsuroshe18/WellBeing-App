package com.example.wellbeing.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

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
import com.example.wellbeing.LoginActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.UpdateAccountDetails;
import com.example.wellbeing.UtilsServices.ConnectivityUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.models.UserModel;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    ArrayList<UserModel> userInfo;
    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    ImageView userProfileCV;
    TextView nameTV, emailTV, userNameTV, dateTV, wellpointTV, rankTV, completedTaskTV, successRateTV;
    AppCompatButton logout;
    NestedScrollView container;
    MaterialButton editBtn;
    ProgressDialog progressDialog;
    LottieAnimationView lottieAnimationView;
    public ProfileFragment() {
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String userName = result.getData().getStringExtra("userName");
                    String profilePic = result.getData().getStringExtra("profilePic");
                    userInfo.get(userInfo.size()-1).setProfilePicture(profilePic);
                    userNameTV.setText(userName.replaceAll("\\s", "").toLowerCase());
                    nameTV.setText(userName);
                    Picasso.get().load(profilePic).into(userProfileCV);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup contain, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, contain, false);

        userInfo = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        container = view.findViewById(R.id.container);
        lottieAnimationView = view.findViewById(R.id.loadingAnim);
        nameTV = view.findViewById(R.id.nameTV);
        userProfileCV = view.findViewById(R.id.profilePictureCV);
        emailTV = view.findViewById(R.id.emailTV);
        userNameTV = view.findViewById(R.id.userNameTV);
        wellpointTV = view.findViewById(R.id.wellpointTV);
        dateTV = view.findViewById(R.id.dateTV);
        rankTV = view.findViewById(R.id.rankTV);
        completedTaskTV = view.findViewById(R.id.completedTaskTV);
        successRateTV = view.findViewById(R.id.successRateTV);
        logout = view.findViewById(R.id.logoutBtn);
        editBtn = view.findViewById(R.id.editBtn);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Register");
        progressDialog.setMessage("Registering to your account");

        editBtn.setOnClickListener(v -> {
            if(!userInfo.isEmpty()){
                Intent intent = new Intent(getContext(), UpdateAccountDetails.class);
                intent.putExtra("userName", nameTV.getText().toString());
                intent.putExtra("profilePic", userInfo.get(userInfo.size()-1).getProfilePicture());
                launcher.launch(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setCancelable(true)
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                logoutUser(); // Proceed to log out
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // Cancel logout
                            }
                        })
                        .show();
            }
        });

        if (!userInfo.isEmpty()){
            Picasso.get().load(userInfo.get(userInfo.size()-1).getProfilePicture()).into(userProfileCV);
            userNameTV.setText(userInfo.get(userInfo.size()-1).getUserName().replaceAll("\\s", "").toLowerCase());
            nameTV.setText(userInfo.get(userInfo.size()-1).getUserName());
            dateTV.setText(userInfo.get(userInfo.size()-1).getCreatedAt());
            emailTV.setText(userInfo.get(userInfo.size()-1).getEmail());
            wellpointTV.setText(userInfo.get(userInfo.size()-1).getWellpoints());
            rankTV.setText(userInfo.get(userInfo.size()-1).getRank());
            completedTaskTV.setText(userInfo.get(userInfo.size()-1).getTask_completed());
            successRateTV.setText(userInfo.get(userInfo.size()-1).getSuccessRate());
        }

        if (ConnectivityUtils.isConnectedToInternet(requireContext())) {
            getUserInfo();
        } else {
            Toast.makeText(getContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }

        return  view;
    }

    public void getUserInfo(){
        container.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);
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

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
                        Date createDate = dateFormat.parse(dataObject.getString("createdAt"));
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String formattedDate = outputFormat.format(createDate);
                        userModel.setCreatedAt(formattedDate);

                        userModel.setWellpoints(String.valueOf(dataObject.getJSONObject("wellpoints").getInt("wellpoints")));
                        userModel.setSuccessRate(String.valueOf(dataObject.getInt("successRate")));
                        int rank = dataObject.getInt("rank")+1;
                        userModel.setRank(String.valueOf(rank));

                        userInfo.add(userModel);
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);

                        Picasso.get().load(userInfo.get(userInfo.size()-1).getProfilePicture()).into(userProfileCV);
                        userNameTV.setText(userInfo.get(userInfo.size()-1).getUserName().replaceAll("\\s", "").toLowerCase());
                        nameTV.setText(userInfo.get(userInfo.size()-1).getUserName());
                        dateTV.setText(userInfo.get(userInfo.size()-1).getCreatedAt());
                        emailTV.setText(userInfo.get(userInfo.size()-1).getEmail());
                        wellpointTV.setText(userInfo.get(userInfo.size()-1).getWellpoints());
                        rankTV.setText(userInfo.get(userInfo.size()-1).getRank());
                        completedTaskTV.setText(userInfo.get(userInfo.size()-1).getTask_completed());
                        successRateTV.setText(userInfo.get(userInfo.size()-1).getSuccessRate());

                        Log.d( "profile Response : ", String.valueOf(dataObject));
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
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
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                            container.setVisibility(View.VISIBLE);
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Unauthorized";
                            container.setVisibility(View.VISIBLE);
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ "Bad request";
                            container.setVisibility(View.VISIBLE);
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                            container.setVisibility(View.VISIBLE);
                            lottieAnimationView.setVisibility(View.INVISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                }
                Log.i("Error", errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                error.printStackTrace();
                container.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }

    public void logoutUser(){
        progressDialog.show();
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/logout";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressDialog.dismiss();
                    String resMsg;
                    if (response != null) {
                        resMsg = response.getString("message");
                        Toast.makeText(getContext(), resMsg, Toast.LENGTH_SHORT).show();
                        sharedPreferenceClass.remove("accessToken");
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        requireActivity().finishAffinity();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(getContext(), "No accessToken found in the response", Toast.LENGTH_SHORT).show();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    String result = null;
                    try {
                        result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                        Log.d("Error : ", result);
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
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
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                }
                Log.i("Error", errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                container.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE);
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

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }
}
package com.example.wellbeing.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.wellbeing.HomeActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.models.UserModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    ArrayList<UserModel> userInfo;
    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    ImageView userProfileCV;
    TextView nameTV, emailTV, userNameTV, dateTV, wellpointTV, rankTV, completedTaskTV, successRateTV;
    FrameLayout container;
    LottieAnimationView lottieAnimationView;
    public ProfileFragment(FrameLayout container, LottieAnimationView lottieAnimationView) {
        // Required empty public constructor
        this.container = container;
        this.lottieAnimationView = lottieAnimationView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userInfo = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        accessToken = sharedPreferenceClass.getValue_string("accessToken");

        getUserInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTV = view.findViewById(R.id.nameTV);
        userProfileCV = view.findViewById(R.id.profilePictureCV);
        emailTV = view.findViewById(R.id.emailTV);
        userNameTV = view.findViewById(R.id.userNameTV);
        wellpointTV = view.findViewById(R.id.wellpointTV);
        dateTV = view.findViewById(R.id.dateTV);
        rankTV = view.findViewById(R.id.rankTV);
        completedTaskTV = view.findViewById(R.id.completedTaskTV);
        successRateTV = view.findViewById(R.id.successRateTV);

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

                        String resMsg = response.getString("message");
                        Toast.makeText(getContext(), resMsg, Toast.LENGTH_SHORT).show();

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

                        userModel.setCreatedAt(String.valueOf(formattedDate));
                        userModel.setWellpoints(String.valueOf(dataObject.getJSONObject("wellpoints").getInt("wellpoints")));
                        userModel.setSuccessRate(String.valueOf(dataObject.getInt("successRate")));
                        userModel.setRank(String.valueOf(dataObject.getInt("rank")+1));

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
}
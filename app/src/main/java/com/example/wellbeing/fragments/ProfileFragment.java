package com.example.wellbeing.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.HomeActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.models.UserModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    ArrayList<UserModel> userInfo;
    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    ImageView userProfileCV;
    TextView nameTV, emailTV, userNameTV, dateTV, wellpointTV, rankTV, completedTaskTV, successRateTV;
    public ProfileFragment() {
        // Required empty public constructor
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

        String apiKey = "http://192.168.186.221:10000/api/v1/users/get-userinfo";

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
                        userModel.setRank(String.valueOf(dataObject.getInt("rank")));

                        userInfo.add(userModel);

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
                        Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle the case when the error or its networkResponse is null
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
    }
}
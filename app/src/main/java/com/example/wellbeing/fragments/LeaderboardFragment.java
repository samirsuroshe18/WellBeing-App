package com.example.wellbeing.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.wellbeing.CommentActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.adapters.CommentAdapter;
import com.example.wellbeing.adapters.LeaderboardAdapter;
import com.example.wellbeing.models.CommentModel;
import com.example.wellbeing.models.LeaderboardModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardFragment extends Fragment {

    ArrayList<LeaderboardModel> leaderboardList;
    LeaderboardAdapter adapter;
    RecyclerView leaderboard_recycler_view;
    CircleImageView firstUser, secondUser, thirdUser;
    TextView firstUserName, secondUserName, thirdUserName, firstUserRank, secondUserRank, thirdUserRank,firstUserPoints, secondUserPoints, thirdUserPoints;

    public LeaderboardFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        leaderboardList = new ArrayList<>();
        getLeaderboardList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        firstUser = view.findViewById(R.id.first_user);
        secondUser = view.findViewById(R.id.second_user);
        thirdUser = view.findViewById(R.id.third_user);
        firstUserName = view.findViewById(R.id.firstUserName);
        secondUserName = view.findViewById(R.id.secondUserName);
        thirdUserName = view.findViewById(R.id.thirdUserName);
        firstUserRank = view.findViewById(R.id.firstUserRank);
        secondUserRank = view.findViewById(R.id.secondUserRank);
        thirdUserRank = view.findViewById(R.id.thirdUserRank);
        firstUserPoints = view.findViewById(R.id.firstUserPoints);
        secondUserPoints = view.findViewById(R.id.secondUserPoints);
        thirdUserPoints = view.findViewById(R.id.thirdUserPoints);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        leaderboard_recycler_view = view.findViewById(R.id.leaderboard_recycler_view);
        leaderboard_recycler_view.setLayoutManager(layoutManager);
        adapter = new LeaderboardAdapter(getContext(), leaderboardList);
        leaderboard_recycler_view.setAdapter(adapter);

        if (leaderboardList.size()==3) {
            LeaderboardModel leaderboardModel = leaderboardList.get(0);
            LeaderboardModel leaderboardModel1 = leaderboardList.get(1);
            LeaderboardModel leaderboardModel2 = leaderboardList.get(2);

            firstUserRank.setText(leaderboardModel.getRankNo());
            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
            firstUserName.setText(leaderboardModel.getUserName());
            firstUserPoints.setText(leaderboardModel.getWellpoints());

            secondUserRank.setText(leaderboardModel1.getRankNo());
            Picasso.get().load(leaderboardModel1.getProfilePicture()).into(secondUser);
            secondUserName.setText(leaderboardModel1.getUserName());
            secondUserPoints.setText(leaderboardModel1.getWellpoints());

            thirdUserRank.setText(leaderboardModel2.getRankNo());
            Picasso.get().load(leaderboardModel2.getProfilePicture()).into(thirdUser);
            thirdUserName.setText(leaderboardModel2.getUserName());
            thirdUserPoints.setText(leaderboardModel2.getWellpoints());
        }

        if (leaderboardList.size()==2) {
            LeaderboardModel leaderboardModel = leaderboardList.get(0);
            LeaderboardModel leaderboardModel1 = leaderboardList.get(1);

            firstUserRank.setText(leaderboardModel.getRankNo());
            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
            firstUserName.setText(leaderboardModel.getUserName());
            firstUserPoints.setText(leaderboardModel.getWellpoints());

            secondUserRank.setText(leaderboardModel1.getRankNo());
            Picasso.get().load(leaderboardModel1.getProfilePicture()).into(secondUser);
            secondUserName.setText(leaderboardModel1.getUserName());
            secondUserPoints.setText(leaderboardModel1.getWellpoints());
        }

        if (leaderboardList.size()==1) {
            LeaderboardModel leaderboardModel = leaderboardList.get(0);

            firstUserRank.setText(leaderboardModel.getRankNo());
            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
            firstUserName.setText(leaderboardModel.getUserName());
            firstUserPoints.setText(leaderboardModel.getWellpoints());
        }


        return view;
    }

    public void getLeaderboardList(){
        String apiKey = "http://192.168.186.221:10000/api/v1/users/get-leaderboardlist";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONArray dataObject = response.getJSONArray("data");

                        for (int i = 0; i<dataObject.length(); i++){

                            LeaderboardModel leaderboardModel = new LeaderboardModel();

                            leaderboardModel.setRankNo(String.valueOf(i+1));
                            leaderboardModel.set_id(dataObject.getJSONObject(i).getString("_id"));
                            leaderboardModel.setUserName(dataObject.getJSONObject(i).getString("userName"));
                            leaderboardModel.setProfilePicture(dataObject.getJSONObject(i).getString("profilePicture"));
                            leaderboardModel.setWellpoints(dataObject.getJSONObject(i).getJSONObject("wellpoints").getString("wellpoints"));

                            leaderboardList.add(leaderboardModel);
                        }

                        adapter.notifyDataSetChanged();

                        if (leaderboardList.size()==3) {
                            LeaderboardModel leaderboardModel = leaderboardList.get(0);
                            LeaderboardModel leaderboardModel1 = leaderboardList.get(1);
                            LeaderboardModel leaderboardModel2 = leaderboardList.get(2);

                            firstUserRank.setText(leaderboardModel.getRankNo());
                            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
                            firstUserName.setText(leaderboardModel.getUserName());
                            firstUserPoints.setText(leaderboardModel.getWellpoints());

                            secondUserRank.setText(leaderboardModel1.getRankNo());
                            Picasso.get().load(leaderboardModel1.getProfilePicture()).into(secondUser);
                            secondUserName.setText(leaderboardModel1.getUserName());
                            secondUserPoints.setText(leaderboardModel1.getWellpoints());

                            thirdUserRank.setText(leaderboardModel2.getRankNo());
                            Picasso.get().load(leaderboardModel2.getProfilePicture()).into(thirdUser);
                            thirdUserName.setText(leaderboardModel2.getUserName());
                            thirdUserPoints.setText(leaderboardModel2.getWellpoints());
                        }

                        if (leaderboardList.size()==2) {
                            LeaderboardModel leaderboardModel = leaderboardList.get(0);
                            LeaderboardModel leaderboardModel1 = leaderboardList.get(1);

                            firstUserRank.setText(leaderboardModel.getRankNo());
                            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
                            firstUserName.setText(leaderboardModel.getUserName());
                            firstUserPoints.setText(leaderboardModel.getWellpoints());

                            secondUserRank.setText(leaderboardModel1.getRankNo());
                            Picasso.get().load(leaderboardModel1.getProfilePicture()).into(secondUser);
                            secondUserName.setText(leaderboardModel1.getUserName());
                            secondUserPoints.setText(leaderboardModel1.getWellpoints());
                        }

                        if (leaderboardList.size()==1) {
                            LeaderboardModel leaderboardModel = leaderboardList.get(0);

                            firstUserRank.setText(leaderboardModel.getRankNo());
                            Picasso.get().load(leaderboardModel.getProfilePicture()).into(firstUser);
                            firstUserName.setText(leaderboardModel.getUserName());
                            firstUserPoints.setText(leaderboardModel.getWellpoints());
                        }


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
                        String err = errRes.getString("message");
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
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
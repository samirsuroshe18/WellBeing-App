package com.example.wellbeing.fragments;

import com.example.wellbeing.UtilsServices.ApiClient;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.wellbeing.R;
import com.example.wellbeing.UtilsServices.ConnectivityUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.models.PostModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    ArrayList<PostModel> postList;
    RecyclerView postRecyclerView;
    PostsAdapter adapter;
    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    ConstraintLayout container;
    LottieAnimationView lottieAnimationView;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup box, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, box, false);

        container = view.findViewById(R.id.container);
        lottieAnimationView = view.findViewById(R.id.loadingAnim);
        postList = new ArrayList<>();
        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        adapter = new PostsAdapter(postList, requireContext(), accessToken);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postRecyclerView = view.findViewById(R.id.post_display_recycler_view);
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(adapter);

        // It checks wheather internet connection is on or not

        if (ConnectivityUtils.isConnectedToInternet(requireContext())) {
            if (postList != null) {
                postList.clear();
            }
            getPosts();
        } else {
            Toast.makeText(getContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
        }
        return  view;
    }

    private void getPosts() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        container.setVisibility(View.INVISIBLE);
        String apiKey = ApiClient.BASE_URL + "/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!isAdded()) return;
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
                if (!isAdded()) return;

                ApiClient.showError(getContext(), error);
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

        RequestQueue requestQueue = ApiClient.getQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

    }

    private void getUpdatedPosts() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        container.setVisibility(View.INVISIBLE);

        String apiKey = ApiClient.BASE_URL + "/upload/get-post";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!isAdded()) return;
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
                if (!isAdded()) return;

                ApiClient.showError(getContext(), error);
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

        RequestQueue requestQueue = ApiClient.getQueue(requireContext());
        requestQueue.add(jsonObjectRequest);

    }

}
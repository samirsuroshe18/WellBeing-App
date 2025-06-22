package com.example.wellbeing.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.wellbeing.PostActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.TaskCompletedActivity;
import com.example.wellbeing.TaskIncompletedActivity;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.models.TaskModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaskFragment extends Fragment {
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;
    private static final String TAG = "TaskFragment";
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    TextView describeTV, timeLeftTV, taskTitleTV, taskCreatedUserName, timelineTV, duration;
    ImageView taskImage, pause, play;
    VideoView taskVideo;
    CircleImageView taskCreatedProfile;
    AppCompatButton nextBtn,acceptBtn, acceptedBtn, postBtn;
    SharedPreferenceClass sharedPreferenceClass;
    String acceptFlag, statusFlag, accessToken, taskId;
    LinearLayout timer;
    ArrayList<TaskModel> tasks;
    ConstraintLayout container;
    ConstraintLayout lottieAnimationView, videoContainer;
    ProgressBar video_progress_bar;
    public TaskFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containe, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, containe, false);

        container = view.findViewById(R.id.container);
        lottieAnimationView = view.findViewById(R.id.loadingOverlay);
        describeTV = view.findViewById(R.id.describeTV);
        taskTitleTV = view.findViewById(R.id.taskTitleTV);
        timelineTV = view.findViewById(R.id.timelineTV);
        taskImage = view.findViewById(R.id.taskImage);
        taskVideo = view.findViewById(R.id.taskVideo);
        taskCreatedProfile = view.findViewById(R.id.taskCreatedProfile);
        taskCreatedUserName = view.findViewById(R.id.taskCreatedUserName);
        nextBtn = view.findViewById(R.id.nextBtn);
        acceptBtn = view.findViewById(R.id.acceptBtn);
        acceptedBtn = view.findViewById(R.id.acceptedBtn);
        postBtn = view.findViewById(R.id.postBtn);
        timeLeftTV = view.findViewById(R.id.timeLeftTV);
        timer = view.findViewById(R.id.timer);
        videoContainer = view.findViewById(R.id.videoContainer);
        video_progress_bar = view.findViewById(R.id.video_progress_bar);
        play = view.findViewById(R.id.play);
        pause = view.findViewById(R.id.pause);
        duration = view.findViewById(R.id.duration);

        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        tasks = new ArrayList<>();

        acceptFlag = sharedPreferenceClass.getValue_string("acceptFlag");
        statusFlag = sharedPreferenceClass.getValue_string("statusFlag");
        accessToken = sharedPreferenceClass.getValue_string("accessToken");

        if (acceptFlag.equals("true")){
            nextBtn.setVisibility(View.INVISIBLE);
            acceptBtn.setVisibility(View.INVISIBLE);
            acceptedBtn.setVisibility(View.VISIBLE);
            postBtn.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);
            getTaskCurrentStatus();
        }else {
            getTask();
        }

        if (statusFlag.equals("completed")){
            startActivity(new Intent(getContext(), TaskCompletedActivity.class));
        }

        if (statusFlag.equals("incompleted")){
            startActivity(new Intent(getContext(), TaskIncompletedActivity.class));
        }

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferenceClass.setValue_string("acceptFlag", "true");
                taskId = tasks.get(tasks.size()-1).get_id();
                sharedPreferenceClass.setValue_string("taskId", taskId);
                nextBtn.setVisibility(View.INVISIBLE);
                acceptBtn.setVisibility(View.INVISIBLE);
                acceptedBtn.setVisibility(View.VISIBLE);
                timer.setVisibility(View.VISIBLE);
                postBtn.setVisibility(View.VISIBLE);
                taskAccepted(taskId);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PostActivity.class));
            }
        });

        describeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lines = describeTV.getMaxLines() + 2;
                describeTV.setMaxLines(lines);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tasks.clear();
                getTask();
            }
        });

        taskVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskVideo.start();
            }
        });

        return  view;

    }

    public void getTask(){
        container.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/usertaskinfo/get-task";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiKey, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONObject dataObject = (JSONObject) response.getJSONArray("data").get(0);
                        TaskModel taskModel = new TaskModel();

                        taskModel.set_id(dataObject.getString("_id"));
                        taskModel.setTitle(dataObject.getString("title"));
                        taskModel.setDescription(dataObject.getString("description"));
                        taskModel.setMediaType(dataObject.getString("mediaType"));
                        taskModel.setTaskReference(dataObject.getString("taskReference"));
                        taskModel.setTimeToComplete(String.valueOf(dataObject.getInt("timeToComplete")));
                        taskModel.setUserId(dataObject.getJSONObject("createdBy").getString("_id"));
                        taskModel.setUserName(dataObject.getJSONObject("createdBy").getString("userName"));
                        taskModel.setPofilePicture(dataObject.getJSONObject("createdBy").getString("profilePicture"));

                        tasks.add(taskModel);
                        sharedPreferenceClass.setValue_string("taskId", dataObject.getString("_id"));
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);

                        taskTitleTV.setText(tasks.get(tasks.size()-1).getTitle());
                        describeTV.setText(tasks.get(tasks.size()-1).getDescription());
                        if (tasks.get(tasks.size()-1).getMediaType().equals("video")){
                            taskImage.setVisibility(View.INVISIBLE);
                            videoContainer.setVisibility(View.VISIBLE);

                            String videoUrl = tasks.get(tasks.size()-1).getTaskReference();
                            Log.d(TAG, "Setting video URL: " + videoUrl);

                            taskVideo.stopPlayback();
                            taskVideo.clearFocus();

                            if (updateTimeRunnable != null) {
                                handler.removeCallbacks(updateTimeRunnable);
                            }

                            taskVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Log.e(TAG, "VideoView Error - What: " + what + ", Extra: " + extra);
                                    Log.e(TAG, "Failed URL: " + videoUrl);
                                    return false;
                                }
                            });

                            taskVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Log.d(TAG, "Video prepared successfully");
                                    // Set video scaling
                                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                                    // Initialize the duration display and progress bar
                                    int totalDuration = taskVideo.getDuration();
                                    updateRemainingTime(totalDuration, totalDuration);
                                    video_progress_bar.setMax(totalDuration);
                                    video_progress_bar.setProgress(0);
                                }
                            });

                            Uri videoUri = Uri.parse(videoUrl);
                            taskVideo.setVideoURI(videoUri);

                            taskVideo.requestFocus();

                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "Play button clicked");

                                    // Check if video is prepared
                                    if (taskVideo.canSeekForward() || taskVideo.canSeekBackward()) {
                                        taskVideo.start();
                                        play.setVisibility(View.INVISIBLE);
                                        pause.setVisibility(View.VISIBLE);

                                        // Start updating remaining time and progress
                                        startUpdatingProgress();
                                        Log.d(TAG, "Video started successfully");
                                    } else {
                                        Log.d(TAG, "Video not ready yet, trying to prepare again");
                                        // Try to reload the video
                                        taskVideo.setVideoURI(videoUri);
                                    }
                                }
                            });

                            pause.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    taskVideo.pause();
                                    Log.d(TAG, "Video pause button click");
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);

                                    // Stop updating progress when paused
                                    if (updateTimeRunnable != null) {
                                        handler.removeCallbacks(updateTimeRunnable);
                                    }
                                }
                            });

                            taskVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);

                                    // Stop updating progress when video completes
                                    if (updateTimeRunnable != null) {
                                        handler.removeCallbacks(updateTimeRunnable);
                                    }

                                    // Reset to show total duration
                                    int totalDuration = taskVideo.getDuration();
                                    updateRemainingTime(totalDuration, totalDuration);
                                    video_progress_bar.setProgress(0);
                                }
                            });
                        }else {
                            taskImage.setVisibility(View.VISIBLE);
                            videoContainer.setVisibility(View.INVISIBLE);
                            Picasso.get().load(tasks.get(tasks.size()-1).getTaskReference()).into(taskImage);
                        }
                        Picasso.get().load(tasks.get(tasks.size()-1).getPofilePicture()).into(taskCreatedProfile);
                        taskCreatedUserName.setText(tasks.get(tasks.size()-1).getUserName());
                        timelineTV.setText(tasks.get(tasks.size()-1).getTimeToComplete());

                        sharedPreferenceClass.setValue_string("List", String.valueOf(tasks.get(tasks.size()-1)));
                        Log.d("getTaskData : ", String.valueOf(dataObject));

                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e){
                    Log.e(TAG, "Exception occured", e);
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
                        Log.e(TAG, "Animation loading failed", e);
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                }
                Log.i("Error", errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    public void taskAccepted(String taskId){
        container.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);

        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/usertaskinfo/accept-task";

        final HashMap<String, String> params = new HashMap<>();
        params.put("taskInfo", taskId);
        params.put("status", "pending");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {

                        JSONObject dataObject = response.getJSONObject("data");
                        sharedPreferenceClass.setValue_string("acceptedTaskId", dataObject.getString("_id"));
                        String remainingTime = dataObject.getString("remainingTime");

                        timeLeftTV.setText(remainingTime);
                        Log.d("acceptedTaskData : ", String.valueOf(dataObject));
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e){
                    Log.e(TAG, "Task Accepted : ", e);
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
                        Log.e(TAG, "Task Accepted : ", e);
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                }
                Log.e(TAG, "Task Accepted : ", error);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                container.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE);

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

    public void getTaskCurrentStatus(){
        container.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.VISIBLE);
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/usertaskinfo/get-status";

        String _id = sharedPreferenceClass.getValue_string("acceptedTaskId");
        final HashMap<String, String> params = new HashMap<>();
        params.put("_id", _id);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response != null) {
                        JSONObject dataObject = response.getJSONObject("data");
                        String status = dataObject.getString("status");
                        Log.d("currentTaskStatusData : ", String.valueOf(dataObject));
                        if (status.equals("incompleted")){
                            sharedPreferenceClass.setValue_string("statusFlag", "incompleted");
                        }

                        String time = dataObject.getString("remainingTime");
                        if (time.contains("-")){
                            sharedPreferenceClass.setValue_string("statusFlag", "incompleted");
                        }

                        timeLeftTV.setText(dataObject.getString("remainingTime"));
                        taskTitleTV.setText(dataObject.getJSONObject("taskInfo").getString("title"));
                        describeTV.setText(dataObject.getJSONObject("taskInfo").getString("description"));

                        String mediaType = dataObject.getJSONObject("taskInfo").getString("mediaType");
                        if (mediaType.equals("video")){
                            taskImage.setVisibility(View.INVISIBLE);
                            videoContainer.setVisibility(View.VISIBLE);

                            String videoUrl = dataObject.getJSONObject("taskInfo").getString("taskReference");
                            Log.d(TAG, "Setting video URL: " + videoUrl);

                            taskVideo.stopPlayback();
                            taskVideo.clearFocus();

                            if (updateTimeRunnable != null) {
                                handler.removeCallbacks(updateTimeRunnable);
                            }

                            taskVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    Log.e(TAG, "VideoView Error - What: " + what + ", Extra: " + extra);
                                    Log.e(TAG, "Failed URL: " + videoUrl);
                                    return false;
                                }
                            });

                            taskVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    Log.d(TAG, "Video prepared successfully");
                                    // Set video scaling
                                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                                    // Initialize the duration display and progress bar
                                    int totalDuration = taskVideo.getDuration();
                                    updateRemainingTime(totalDuration, totalDuration);
                                    video_progress_bar.setMax(totalDuration);
                                    video_progress_bar.setProgress(0);
                                }
                            });

                            Uri videoUri = Uri.parse(videoUrl);
                            taskVideo.setVideoURI(videoUri);

                            taskVideo.requestFocus();

                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Log.d(TAG, "Play button clicked");

                                    // Check if video is prepared
                                    if (taskVideo.canSeekForward() || taskVideo.canSeekBackward()) {
                                        taskVideo.start();
                                        play.setVisibility(View.INVISIBLE);
                                        pause.setVisibility(View.VISIBLE);

                                        // Start updating remaining time and progress
                                        startUpdatingProgress();
                                        Log.d(TAG, "Video started successfully");
                                    } else {
                                        Log.d(TAG, "Video not ready yet, trying to prepare again");
                                        // Try to reload the video
                                        taskVideo.setVideoURI(videoUri);
                                    }
                                }
                            });

                            pause.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    taskVideo.pause();
                                    Log.d(TAG, "Video pause button click");
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);

                                    // Stop updating progress when paused
                                    if (updateTimeRunnable != null) {
                                        handler.removeCallbacks(updateTimeRunnable);
                                    }
                                }
                            });

                            taskVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);

                                    // Stop updating progress when video completes
                                    if (updateTimeRunnable != null) {
                                        handler.removeCallbacks(updateTimeRunnable);
                                    }

                                    // Reset to show total duration
                                    int totalDuration = taskVideo.getDuration();
                                    updateRemainingTime(totalDuration, totalDuration);
                                    video_progress_bar.setProgress(0);
                                }
                            });
//                            taskVideo.setVideoURI(Uri.parse(dataObject.getJSONObject("taskInfo").getString("taskReference")));
                        }else {
                            taskImage.setVisibility(View.VISIBLE);
                            videoContainer.setVisibility(View.INVISIBLE);
                            Picasso.get().load(dataObject.getJSONObject("taskInfo").getString("taskReference")).into(taskImage);
                        }

                        Picasso.get().load(dataObject.getJSONObject("taskInfo").getJSONObject("createdBy").getString("profilePicture")).into(taskCreatedProfile);
                        taskCreatedUserName.setText(dataObject.getJSONObject("taskInfo").getJSONObject("createdBy").getString("userName"));
                        timelineTV.setText(String.valueOf(dataObject.getJSONObject("taskInfo").getInt("timeToComplete")));


                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    } else {
                        // Handle the case where "accessToken" key is not present in the JSON response
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e){
                    Log.e(TAG, "Get Task Current Status : ", e);
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
                        Log.e(TAG, "Get Task Current status : ", e);
                        container.setVisibility(View.VISIBLE);
                        lottieAnimationView.setVisibility(View.INVISIBLE);
                    }
                    container.setVisibility(View.VISIBLE);
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                }
                Log.e(TAG, "Get Task Current status : ", error);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                container.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.INVISIBLE);

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

    private void startUpdatingProgress() {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (taskVideo.isPlaying()) {
                        int currentPosition = taskVideo.getCurrentPosition();
                        int totalDuration = taskVideo.getDuration();
                        int remainingTime = totalDuration - currentPosition;

                        // Update remaining time display
                        updateRemainingTime(remainingTime, totalDuration);

                        // Update progress bar
                        video_progress_bar.setProgress(currentPosition);

                        // Schedule next update
                        handler.postDelayed(this, 1000); // Update every second
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.post(updateTimeRunnable);
    }

    private void updateRemainingTime(int remainingMillis, int totalMillis) {
        if (remainingMillis <= 0) {
            duration.setText("0s");
            return;
        }

        String remainingText = "-" + formatDuration(remainingMillis);
        duration.setText(remainingText);
    }

    private String formatDuration(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds % 60);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable);
        }
    }
}
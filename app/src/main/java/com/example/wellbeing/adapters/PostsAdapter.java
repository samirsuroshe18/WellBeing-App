package com.example.wellbeing.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
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
import com.example.wellbeing.AcceptedTaskActivity;
import com.example.wellbeing.CommentActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.models.PostModel;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Handler handler = new Handler();
    private Runnable updateTimeRunnable;
    private static final String TAG = "PostsAdapter";
    ArrayList<PostModel> postModel;
    Context context;
    int IMAGE_VIEW_TYPE = 0;
    int VIDEO_VIEW_TYPE = 1;
    String accessToken;
    PostModel posts;
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;

    public PostsAdapter(ArrayList<PostModel> postModel, Context context, String accessToken) {
        this.postModel = postModel;
        this.context = context;
        this.accessToken = accessToken;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == IMAGE_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.img_posts_recycler_layout, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.vid_post_recycler_layout, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        posts = postModel.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        if (holder.getClass() == ImageViewHolder.class) {

            try {
                Picasso.get().load(posts.getUserProfile()).into(((ImageViewHolder) holder).user_profile);
                Picasso.get().load(posts.getMedia()).into(((ImageViewHolder) holder).post_image);
                ((ImageViewHolder) holder).user_name.setText(posts.getUserName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set time zone to UTC
                Date date = sdf.parse(posts.getCreatedAt());
                Log.d("onBindViewHolder: ", String.valueOf(date));
                PrettyTime prettyTime = new PrettyTime();
                ((ImageViewHolder) holder).time.setText(prettyTime.format(date));
                ((ImageViewHolder) holder).description.setText(posts.getDescription());
                ((ImageViewHolder) holder).like_count.setText(String.valueOf(posts.getTotalLikes()));
                ((ImageViewHolder) holder).dislike_count.setText(String.valueOf(posts.getTotalDislikes()));
                ((ImageViewHolder) holder).comment_count.setText(String.valueOf(posts.getTotalComments()));
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
            }

            ((ImageViewHolder) holder).description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int lines = ((ImageViewHolder) holder).description.getMaxLines() + 2;
                    ((ImageViewHolder) holder).description.setMaxLines(lines);
                }
            });

            ((ImageViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/like/send-like";
                    PostModel currentPost = postModel.get(holder.getAdapterPosition());

                    final HashMap<String, String> params = new HashMap<>();
                    params.put("multiMedia", currentPost.get_id());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    currentPost.setTotalLikes(dataObject.getInt("totalLikes"));
                                    postModel.set(holder.getAdapterPosition(), currentPost);
                                    String resMsg = response.getString("message");
                                    Toast.makeText(context, resMsg, Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(holder.getAdapterPosition());
                                } else {
                                    // Handle the case where "accessToken" key is not present in the JSON response
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            TIMEOUT_MS,
                            MAX_RETRIES,
                            BACKOFF_MULT
                    ));
                }
            });

            ((ImageViewHolder) holder).dislike_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/dislike/send-dislike";
                    PostModel currentPost = postModel.get(holder.getAdapterPosition());

                    final HashMap<String, String> params = new HashMap<>();
                    params.put("multiMedia", currentPost.get_id());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    currentPost.setTotalDislikes(dataObject.getInt("totalDislike"));
                                    postModel.set(holder.getAdapterPosition(), currentPost);
                                    notifyItemChanged(holder.getAdapterPosition());
                                    String resMsg = response.getString("message");
                                    Toast.makeText(context, resMsg, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Handle the case where "accessToken" key is not present in the JSON response
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            TIMEOUT_MS,
                            MAX_RETRIES,
                            BACKOFF_MULT
                    ));
                }
            });

            ((ImageViewHolder) holder).comment_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PostModel currentPost = postModel.get(holder.getAdapterPosition());
                    String _id = currentPost.get_id();
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("_id", _id);
                    intent.putExtra("accessToken", accessToken);
                    context.startActivity(intent);
                }
            });

            ((ImageViewHolder) holder).view_img_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AcceptedTaskActivity.class);
                    intent.putExtra("post_id", posts.getTaskId());
                    context.startActivity(intent);
                }
            });
        } else {
// Your existing code with updates
            String videoUrl = posts.getMedia();
            Log.d(TAG, "Setting video URL: " + videoUrl);

// Reset the VideoView first
            ((VideoViewHolder) holder).post_video.stopPlayback();
            ((VideoViewHolder) holder).post_video.clearFocus();

// Stop any existing timer for this holder
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }

            ((VideoViewHolder) holder).post_video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "VideoView Error - What: " + what + ", Extra: " + extra);
                    Log.e(TAG, "Failed URL: " + videoUrl);
                    return false;
                }
            });

            ((VideoViewHolder) holder).post_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "Video prepared successfully");
                    // Set video scaling
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);

                    // Initialize the duration display and progress bar
                    int totalDuration = ((VideoViewHolder) holder).post_video.getDuration();
                    updateRemainingTime((VideoViewHolder) holder, totalDuration, totalDuration);
                    ((VideoViewHolder) holder).video_progress_bar.setMax(totalDuration);
                    ((VideoViewHolder) holder).video_progress_bar.setProgress(0);
                }
            });

// Now set the video
            Uri videoUri = Uri.parse(videoUrl);
            ((VideoViewHolder) holder).post_video.setVideoURI(videoUri);

// Request focus to ensure it's ready
            ((VideoViewHolder) holder).post_video.requestFocus();

            Picasso.get().load(posts.getUserProfile()).into(((VideoViewHolder) holder).user_profile);
            ((VideoViewHolder) holder).user_name.setText(posts.getUserName());
            ((VideoViewHolder) holder).user_name.setText(posts.getUserName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = null;

            try {
                date = sdf.parse(posts.getCreatedAt());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            PrettyTime prettyTime = new PrettyTime();
            ((VideoViewHolder) holder).time.setText(prettyTime.format(date));
            ((VideoViewHolder) holder).description.setText(posts.getDescription());
            ((VideoViewHolder) holder).like_count.setText(String.valueOf(posts.getTotalLikes()));
            ((VideoViewHolder) holder).dislike_count.setText(String.valueOf(posts.getTotalDislikes()));
            ((VideoViewHolder) holder).comment_count.setText(String.valueOf(posts.getTotalComments()));

// Initial duration setup - show total duration until video starts
            int duration1 = Integer.parseInt(posts.getDuration());
            String initialDurationText = formatDuration(duration1 * 1000); // Convert to milliseconds
            ((VideoViewHolder) holder).duration.setText(initialDurationText);

            ((VideoViewHolder) holder).play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Play button clicked");

                    // Check if video is prepared
                    if (((VideoViewHolder) holder).post_video.canSeekForward() ||
                            ((VideoViewHolder) holder).post_video.canSeekBackward()) {
                        ((VideoViewHolder) holder).post_video.start();
                        ((VideoViewHolder) holder).play.setVisibility(View.INVISIBLE);
                        ((VideoViewHolder) holder).pause.setVisibility(View.VISIBLE);

                        // Start updating remaining time and progress
                        startUpdatingProgress((VideoViewHolder)holder);
                        Log.d(TAG, "Video started successfully");
                    } else {
                        Log.d(TAG, "Video not ready yet, trying to prepare again");
                        // Try to reload the video
                        ((VideoViewHolder) holder).post_video.setVideoURI(Uri.parse(posts.getMedia()));
                    }
                }
            });

            ((VideoViewHolder) holder).pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((VideoViewHolder) holder).post_video.pause();
                    Log.d(TAG, "Video pause button click");
                    ((VideoViewHolder) holder).play.setVisibility(View.VISIBLE);
                    ((VideoViewHolder) holder).pause.setVisibility(View.INVISIBLE);

                    // Stop updating progress when paused
                    if (updateTimeRunnable != null) {
                        handler.removeCallbacks(updateTimeRunnable);
                    }
                }
            });

            ((VideoViewHolder) holder).description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int lines = ((VideoViewHolder) holder).description.getMaxLines() + 2;
                    ((VideoViewHolder) holder).description.setMaxLines(lines);
                }
            });

// Your existing like button click listener code remains the same...
            ((VideoViewHolder) holder).like_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/like/send-like";
                    PostModel currentPost = postModel.get(holder.getAdapterPosition());

                    final HashMap<String, String> params = new HashMap<>();
                    params.put("multiMedia", currentPost.get_id());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    currentPost.setTotalLikes(dataObject.getInt("totalLikes"));
                                    postModel.set(holder.getAdapterPosition(), currentPost);
                                    String resMsg = response.getString("message");
                                    Toast.makeText(context, resMsg, Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(holder.getAdapterPosition());
                                } else {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            TIMEOUT_MS,
                            MAX_RETRIES,
                            BACKOFF_MULT
                    ));
                }
            });

// Your existing dislike button click listener code remains the same...
            ((VideoViewHolder) holder).dislike_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/dislike/send-dislike";
                    PostModel currentPost = postModel.get(holder.getAdapterPosition());

                    final HashMap<String, String> params = new HashMap<>();
                    params.put("multiMedia", currentPost.get_id());

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiKey, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response != null) {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    currentPost.setTotalDislikes(dataObject.getInt("totalDislike"));
                                    postModel.set(holder.getAdapterPosition(), currentPost);
                                    notifyItemChanged(holder.getAdapterPosition());
                                    String resMsg = response.getString("message");
                                    Toast.makeText(context, resMsg, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
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

                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            TIMEOUT_MS,
                            MAX_RETRIES,
                            BACKOFF_MULT
                    ));
                }
            });

            ((VideoViewHolder) holder).comment_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CommentActivity.class);
                    context.startActivity(intent);
                }
            });

            ((VideoViewHolder) holder).post_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    ((VideoViewHolder) holder).play.setVisibility(View.VISIBLE);
                    ((VideoViewHolder) holder).pause.setVisibility(View.INVISIBLE);

                    // Stop updating progress when video completes
                    if (updateTimeRunnable != null) {
                        handler.removeCallbacks(updateTimeRunnable);
                    }

                    // Reset to show total duration
                    int totalDuration = ((VideoViewHolder) holder).post_video.getDuration();
                    updateRemainingTime((VideoViewHolder) holder, totalDuration, totalDuration);
                    ((VideoViewHolder) holder).video_progress_bar.setProgress(0);
                }
            });

            ((VideoViewHolder) holder).view_vid_task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AcceptedTaskActivity.class);
                    intent.putExtra("post_id", posts.getTaskId());
                    context.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (postModel.get(position).getMediaType().equals("image")) {

            return IMAGE_VIEW_TYPE;
        } else {

            return VIDEO_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return postModel.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user_profile;
        TextView user_name, time, description, like_count, dislike_count, comment_count;
        ImageView post_image, like_icon, dislike_icon, comment_icon;
        MaterialButton view_img_task;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_profile);
            user_name = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            description = itemView.findViewById(R.id.description);
            like_count = itemView.findViewById(R.id.like_count);
            dislike_count = itemView.findViewById(R.id.dislike_count);
            comment_count = itemView.findViewById(R.id.comment_count);
            post_image = itemView.findViewById(R.id.post_image);
            like_icon = itemView.findViewById(R.id.like_icon);
            dislike_icon = itemView.findViewById(R.id.dislike_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            view_img_task = itemView.findViewById(R.id.view_img_task);
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        CircleImageView user_profile;
        TextView user_name, time, description, like_count, dislike_count, comment_count, duration;
        ImageView like_icon, dislike_icon, comment_icon, play, pause;
        VideoView post_video;
        ProgressBar video_progress_bar;
        MaterialButton view_vid_task;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_profile);
            user_name = itemView.findViewById(R.id.user_name);
            time = itemView.findViewById(R.id.time);
            description = itemView.findViewById(R.id.description);
            like_count = itemView.findViewById(R.id.like_count);
            dislike_count = itemView.findViewById(R.id.dislike_count);
            comment_count = itemView.findViewById(R.id.comment_count);
            post_video = itemView.findViewById(R.id.post_video);
            like_icon = itemView.findViewById(R.id.like_icon);
            dislike_icon = itemView.findViewById(R.id.dislike_icon);
            comment_icon = itemView.findViewById(R.id.comment_icon);
            play = itemView.findViewById(R.id.play);
            pause = itemView.findViewById(R.id.pause);
            duration = itemView.findViewById(R.id.duration);
            video_progress_bar = itemView.findViewById(R.id.video_progress_bar);
            view_vid_task = itemView.findViewById(R.id.view_vid_task);
        }
    }

    private void startUpdatingProgress(VideoViewHolder holder) {
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (holder.post_video.isPlaying()) {
                        int currentPosition = holder.post_video.getCurrentPosition();
                        int totalDuration = holder.post_video.getDuration();
                        int remainingTime = totalDuration - currentPosition;

                        // Update remaining time display
                        updateRemainingTime(holder, remainingTime, totalDuration);

                        // Update progress bar
                        holder.video_progress_bar.setProgress(currentPosition);

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

    private void updateRemainingTime(VideoViewHolder holder, int remainingMillis, int totalMillis) {
        if (remainingMillis <= 0) {
            holder.duration.setText("0s");
            return;
        }

        String remainingText = "-" + formatDuration(remainingMillis);
        holder.duration.setText(remainingText);
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

    // Don't forget to add cleanup in your adapter to prevent memory leaks
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            // Stop any running timers
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }
            // Stop video playback
            ((VideoViewHolder) holder).post_video.stopPlayback();
        }
    }
}

package com.example.wellbeing.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.CommentActivity;
import com.example.wellbeing.HomeActivity;
import com.example.wellbeing.R;
import com.example.wellbeing.RegisterActivity;
import com.example.wellbeing.models.PostModel;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter {

    ArrayList<PostModel> postModel;
    Context context;
    int IMAGE_VIEW_TYPE = 0;
    int VIDEO_VIEW_TYPE = 1;
    String accessToken;
    PostModel posts;

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
                Picasso.get().load(posts.getUserProfile()).placeholder(R.drawable.avatar).into(((ImageViewHolder) holder).user_profile);
                Picasso.get().load(posts.getMedia()).into(((ImageViewHolder) holder).post_image);
                ((ImageViewHolder) holder).user_name.setText(posts.getUserName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = sdf.parse(posts.getCreatedAt());
                PrettyTime prettyTime = new PrettyTime();
                ((ImageViewHolder) holder).time.setText(prettyTime.format(date));
                ((ImageViewHolder) holder).description.setText(posts.getDescription());
                ((ImageViewHolder) holder).like_count.setText(String.valueOf(posts.getTotalLikes()));
                ((ImageViewHolder) holder).dislike_count.setText(String.valueOf(posts.getTotalDislikes()));
                ((ImageViewHolder) holder).comment_count.setText(String.valueOf(posts.getTotalComments()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

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
                            if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                try {
                                    JSONObject errRes = new JSONObject(errMsg);
                                    String err = errRes.getString("error");
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Handle the case when the error or its networkResponse is null
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

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
                            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                                    currentPost.setTotalDislikes(dataObject.getInt("totalDislikes"));
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
                            if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                try {
                                    JSONObject errRes = new JSONObject(errMsg);
                                    String err = errRes.getString("error");
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Handle the case when the error or its networkResponse is null
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

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
                            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
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
        } else {

            Picasso.get().load(posts.getUserProfile()).into(((VideoViewHolder) holder).user_profile);
            ((VideoViewHolder) holder).post_video.setVideoURI(Uri.parse(posts.getMedia()));
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
            int duration1 = Integer.parseInt(posts.getDuration());
            if (duration1>60){
                int time = duration1/60;
                DecimalFormat df = new DecimalFormat("#.##");
                String duration = df.format(time)+" M";
                ((VideoViewHolder) holder).duration.setText(duration);
            }else {
                String duration = posts.getDuration()+" S";
                ((VideoViewHolder) holder).duration.setText(duration);
            }

            ((VideoViewHolder) holder).play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((VideoViewHolder) holder).post_video.start();
                    ((VideoViewHolder) holder).play.setVisibility(View.INVISIBLE);
                    ((VideoViewHolder) holder).pause.setVisibility(View.VISIBLE);
                }
            });

            ((VideoViewHolder) holder).pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((VideoViewHolder) holder).post_video.pause();
                    ((VideoViewHolder) holder).play.setVisibility(View.VISIBLE);
                    ((VideoViewHolder) holder).pause.setVisibility(View.INVISIBLE);
                }
            });

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
                            if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                try {
                                    JSONObject errRes = new JSONObject(errMsg);
                                    String err = errRes.getString("error");
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Handle the case when the error or its networkResponse is null
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

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
                            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    ));
                }
            });

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
                                    currentPost.setTotalDislikes(dataObject.getInt("totalDislikes"));
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
                            if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
                                String errMsg = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                try {
                                    JSONObject errRes = new JSONObject(errMsg);
                                    String err = errRes.getString("error");
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Handle the case when the error or its networkResponse is null
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

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
                            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
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

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user_profile;
        TextView user_name, time, description, like_count, dislike_count, comment_count;
        ImageView post_image, like_icon, dislike_icon, comment_icon;

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

        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        CircleImageView user_profile;
        TextView user_name, time, description, like_count, dislike_count, comment_count, duration;
        ImageView like_icon, dislike_icon, comment_icon, play, pause;
        VideoView post_video;

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


        }
    }

}

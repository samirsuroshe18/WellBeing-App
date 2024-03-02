package com.example.wellbeing.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wellbeing.R;
import com.example.wellbeing.models.PostModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter {

    ArrayList<PostModel> postModel;
    Context context;
    int IMAGE_VIEW_TYPE = 0;
    int VIDEO_VIEW_TYPE = 1;

    public PostsAdapter(ArrayList<PostModel> postModel, Context context) {
        this.postModel = postModel;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == IMAGE_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.img_posts_recycler_layout, parent, false);
            return new ImageViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.vid_post_recycler_layout, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        PostModel posts = postModel.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        if (holder.getClass() == ImageViewHolder.class){
            Log.d("testing", posts.getMediaType());
            ((ImageViewHolder)holder).user_profile.setImageURI(Uri.parse(posts.getUserProfile()));
            ((ImageViewHolder)holder).post_image.setImageURI(Uri.parse(posts.getMedia()));
            ((ImageViewHolder)holder).user_name.setText(posts.getUserName());
            ((ImageViewHolder)holder).time.setText(posts.getCreatedAt());
            ((ImageViewHolder)holder).description.setText(posts.getDescription());
            ((ImageViewHolder)holder).like_count.setText(posts.getTotalLikes());
            ((ImageViewHolder)holder).dislike_count.setText(posts.getTotalDislikes());
            ((ImageViewHolder)holder).comment_count.setText(posts.getTotalComments());
        }
        else {
            Log.d("testing", posts.getMediaType());
            ((VideoViewHolder)holder).user_profile.setImageURI(Uri.parse(posts.getUserProfile()));
            ((VideoViewHolder)holder).post_video.setVideoURI(Uri.parse(posts.getMedia()));
            ((VideoViewHolder)holder).user_name.setText(posts.getUserName());
            ((VideoViewHolder)holder).time.setText(posts.getCreatedAt());
            ((VideoViewHolder)holder).description.setText(posts.getDescription());
            ((VideoViewHolder)holder).like_count.setText(posts.getTotalLikes());
            ((VideoViewHolder)holder).dislike_count.setText(posts.getTotalDislikes());
            ((VideoViewHolder)holder).comment_count.setText(posts.getTotalComments());

            ((VideoViewHolder)holder).play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((VideoViewHolder)holder).post_video.start();
                    ((VideoViewHolder)holder).play.setVisibility(View.GONE);
                    ((VideoViewHolder)holder).pause.setVisibility(View.VISIBLE);
                }
            });

            ((VideoViewHolder)holder).pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((VideoViewHolder)holder).post_video.pause();
                    ((VideoViewHolder)holder).play.setVisibility(View.VISIBLE);
                    ((VideoViewHolder)holder).pause.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (postModel.get(position).getMediaType().equals("image")){
            return IMAGE_VIEW_TYPE;
        }
        else {
            return VIDEO_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return postModel.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
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

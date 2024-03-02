package com.example.wellbeing.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.wellbeing.R;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.models.PostModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    ArrayList<PostModel> postList;
    RecyclerView postRecyclerView;
    ImageView profile;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        profile = view.findViewById(R.id.user_profile);
        postList = new ArrayList<>();
        postRecyclerView = view.findViewById(R.id.post_display_recycler_view);

        String vid_post = "android.resource://"+ getActivity().getPackageName()+"/"+R.raw.testingvideo;
        String img_post= "android.resource://"+getActivity().getPackageName()+"/"+R.drawable.avatar;
        String userProfile = "android.resource://"+getActivity().getPackageName()+"/"+R.drawable.user;
        String userName = "Samir Suroshe";
        String createdAt = "10s";
        String description = "skldfjgfjdklg\nkfdjlgs\nfjsdkg\nfdsfjhgk\nhdfskjg\ndfg\nfdfg";
        String totalLikes = "12";
        String totalDislike = "35";
        String totalComments = "65";
        String mediaType = "image";
        String vidMediaType = "video";

        profile.setImageURI(Uri.parse(img_post));

        postList.add(new PostModel("1", userProfile, userName, createdAt, description, img_post, totalLikes, totalDislike, totalComments, mediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, vid_post, totalLikes, totalDislike, totalComments, vidMediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, img_post, totalLikes, totalDislike, totalComments, mediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, vid_post, totalLikes, totalDislike, totalComments, vidMediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, img_post, totalLikes, totalDislike, totalComments, mediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, vid_post, totalLikes, totalDislike, totalComments, vidMediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, img_post, totalLikes, totalDislike, totalComments, mediaType));
        postList.add(new PostModel("1", userProfile, userName, createdAt, description, vid_post, totalLikes, totalDislike, totalComments, vidMediaType));


        PostsAdapter adapter = new PostsAdapter(postList, getContext());
        postRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postRecyclerView.setLayoutManager(layoutManager);

        return  view;
    }
}
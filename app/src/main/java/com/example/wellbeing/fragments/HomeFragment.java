package com.example.wellbeing.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wellbeing.R;
import com.example.wellbeing.adapters.PostsAdapter;
import com.example.wellbeing.models.PostModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    ArrayList<PostModel> postList;
    RecyclerView postRecyclerView;
    PostsAdapter adapter;

    public HomeFragment() {
    }

    public  HomeFragment(ArrayList<PostModel> postList, PostsAdapter adapter){
        this.postList = postList;
        this.adapter = adapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postRecyclerView = view.findViewById(R.id.post_display_recycler_view);
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(adapter);

        return  view;
    }


}
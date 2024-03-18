package com.example.wellbeing.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wellbeing.R;
import com.example.wellbeing.models.LeaderboardModel;
import com.example.wellbeing.models.PostModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    Context context;
    ArrayList<LeaderboardModel> leaderboardList;

    public LeaderboardAdapter(Context context, ArrayList<LeaderboardModel> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_list_recy_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardModel leaderboardModel = leaderboardList.get(position);

        holder.rankNo.setText(leaderboardModel.getRankNo());
        holder.userName.setText(leaderboardModel.getUserName());
        holder.wellpoints.setText(leaderboardModel.getWellpoints());
        Picasso.get().load(leaderboardModel.getProfilePicture()).into(holder.profilePicture);
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView rankNo, userName, wellpoints;
        CircleImageView profilePicture;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rankNo = itemView.findViewById(R.id.rankNo);
            userName = itemView.findViewById(R.id.userName);
            wellpoints = itemView.findViewById(R.id.wellpoints);
            profilePicture = itemView.findViewById(R.id.profilePicture);
        }
    }

}

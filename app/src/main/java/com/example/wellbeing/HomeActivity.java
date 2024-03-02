package com.example.wellbeing;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wellbeing.fragments.CreateFragment;
import com.example.wellbeing.fragments.HomeFragment;
import com.example.wellbeing.fragments.LeaderboardFragment;
import com.example.wellbeing.fragments.ProfileFragment;
import com.example.wellbeing.fragments.TaskFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNav);

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        loadFrag(new HomeFragment(), true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id==R.id.navigation_home){
                    loadFrag(new HomeFragment(), false);
                } else if (id==R.id.navigation_task) {
                    loadFrag(new TaskFragment(), false);
                } else if (id==R.id.navigation_create) {
                    loadFrag(new CreateFragment(), false);
                } else if (id==R.id.navigation_leaderboard) {
                    loadFrag(new LeaderboardFragment(), false);
                }else {
                    loadFrag(new ProfileFragment(), false);
                }

                return true;
            }
        });
    }

    public void loadFrag(Fragment fragment, boolean flag){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (flag){
            fragmentTransaction.add(R.id.container, fragment);
        }else {
            fragmentTransaction.replace(R.id.container, fragment);
        }

        fragmentTransaction.commit();
    }

}
package com.example.wellbeing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class PostActivity extends AppCompatActivity {

    ImageView image, video, send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        image = findViewById(R.id.image);
        video = findViewById(R.id.video);
        send = findViewById(R.id.upload_post);

    }
}
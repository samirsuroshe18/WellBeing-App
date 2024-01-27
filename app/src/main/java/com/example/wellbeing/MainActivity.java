package com.example.wellbeing;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.wellbeing.UtilsServices.SharedPreferenceClass;

public class MainActivity extends AppCompatActivity {

    String accessToken;
    SharedPreferenceClass sharedPreferenceClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessToken.isEmpty()){
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
            }
        },3000);
    }
}
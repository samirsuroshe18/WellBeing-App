package com.example.wellbeing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wellbeing.UtilsServices.SharedPreferenceClass;

public class TaskCompletedActivity extends AppCompatActivity {

    Button selectTaskBtn;
    SharedPreferenceClass sharedPreferenceClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_completed);

        selectTaskBtn = findViewById(R.id.selectTaskBtn);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        Toast.makeText(TaskCompletedActivity.this, "This is completed Activity", Toast.LENGTH_SHORT).show();

        selectTaskBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sharedPreferenceClass.setValue_string("acceptFlag", "false");
                sharedPreferenceClass.setValue_string("statusFlag", "");
                Intent intent = new Intent(TaskCompletedActivity.this, HomeActivity.class);
                intent.putExtra("fragmentTagName", "taskFragment");
                startActivity(intent);
                finish();
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Intent intent = new Intent(TaskCompletedActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


}
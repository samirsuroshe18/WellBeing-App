package com.example.wellbeing;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private MaterialButton resetPasswordBtn;
    private ProgressBar resetProgress;
    private TextView backToLoginLink;

    // Volley
    private RequestQueue requestQueue;

    // API Configuration
    private static final String FORGOT_PASSWORD_URL = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/forgot"; // Replace with your API endpoint

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.email_input_layout);
        emailEditText = findViewById(R.id.email_editText);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
        resetProgress = findViewById(R.id.reset_progress);
        backToLoginLink = findViewById(R.id.back_to_login_link);
    }

    private void setupClickListeners() {
        // Reset password button click
        resetPasswordBtn.setOnClickListener(v -> {
            handleForgotPassword();
        });

        // Back to login link click
        backToLoginLink.setOnClickListener(v -> {
            // Navigate back to login activity
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();

        // Clear previous errors
        emailInputLayout.setError(null);

        // Validate input
        if (!validateInput(email)) {
            return;
        }

        // Show loading state
        showLoading(true);

        // Make API request
        sendForgotPasswordRequest(email);
    }

    private boolean validateInput(String email) {
        // Check if email is empty
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }

        // Check if email format is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void sendForgotPasswordRequest(String email) {
        // Create JSON object for the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            showLoading(false);
            showToast("Error creating request");
            return;
        }

        // Create the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                FORGOT_PASSWORD_URL,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showLoading(false);
                        handleApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showLoading(false);
                        handleApiError(error);
                    }
                }
        );

        // Add request timeout (optional)
        jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, // 30 seconds timeout
                0, // No retries
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }

    private void handleApiResponse(JSONObject response) {
        try {
            // Parse the response based on your API structure
            boolean success = response.getBoolean("success"); // Adjust based on your API response format
            String message = response.getString("message");

            if (success) {
                showToast("Reset link sent successfully! Check your email.");

                // Optional: Clear the email field
                emailEditText.setText("");

                // Optional: Navigate back to login after success
                // finish();
            } else {
                showToast(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error parsing response");
        }
    }

    private void handleApiError(VolleyError error) {
        String errorMessage = "Network error occurred";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;

            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject errorResponse = new JSONObject(responseBody);

                // Parse error message from API response
                if (errorResponse.has("message")) {
                    errorMessage = errorResponse.getString("message");
                } else if (errorResponse.has("error")) {
                    errorMessage = errorResponse.getString("error");
                }
            } catch (Exception e) {
                // Handle different status codes
                switch (statusCode) {
                    case 400:
                        errorMessage = "Invalid email address";
                        break;
                    case 404:
                        errorMessage = "Email not found in our records";
                        break;
                    case 429:
                        errorMessage = "Too many requests. Please try again later";
                        break;
                    case 500:
                        errorMessage = "Server error. Please try again later";
                        break;
                    default:
                        errorMessage = "Something went wrong. Please try again";
                        break;
                }
            }
        }

        showToast(errorMessage);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            resetPasswordBtn.setText("");
            resetPasswordBtn.setEnabled(false);
            resetProgress.setVisibility(View.VISIBLE);
        } else {
            resetPasswordBtn.setText("Send Reset Link");
            resetPasswordBtn.setEnabled(true);
            resetProgress.setVisibility(View.GONE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}
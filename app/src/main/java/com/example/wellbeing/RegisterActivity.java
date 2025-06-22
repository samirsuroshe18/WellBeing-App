package com.example.wellbeing;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.UtilsServices.FileUtils;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.UtilsServices.UriToByteArrayConverterUtil;
import com.example.wellbeing.UtilsServices.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    TextView mov_to_logIn, forgot_password;
    EditText email_editText, name_editText, pass_editText;
    Button sign_up_btn;
    String name, email, password, accessToken, refreshToken, fileName, fileType;
    SharedPreferenceClass sharedPreference;
    ProgressDialog progressDialog;
    ProgressBar login_progress;
    ImageView plusIconIv;
    CircleImageView circleImageView;
    byte[] multiMediaByteArray;
    Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Register");
        progressDialog.setMessage("Registering to your account");
        mov_to_logIn = findViewById(R.id.mov_to_logIn);
        forgot_password = findViewById(R.id.forgot_password);
        email_editText = findViewById(R.id.email_editText);
        name_editText = findViewById(R.id.name_editText);
        pass_editText = findViewById(R.id.pass_editText);
        sign_up_btn = findViewById(R.id.sign_in_btn);
        login_progress = findViewById(R.id.login_progress);
        plusIconIv = findViewById(R.id.plusIconIV);
        circleImageView = findViewById(R.id.circleImageView);
        sharedPreference = new SharedPreferenceClass(RegisterActivity.this);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                multiMediaByteArray = UriToByteArrayConverterUtil.convertUriToByteArray(getApplicationContext(), selectedImageUri);
                                fileName = getFileName(selectedImageUri);
                                fileType = getFileType(selectedImageUri);
                                circleImageView.setImageURI(selectedImageUri);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
        );

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });

        plusIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        mov_to_logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pattern = Pattern.compile(regex);
                name = name_editText.getText().toString();
                email = email_editText.getText().toString();
                password = pass_editText.getText().toString();
                Matcher matcher = pattern.matcher(email);
                if (name.isEmpty() && email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!!", Toast.LENGTH_SHORT).show();
                }else if(selectedImageUri == null){
                    Toast.makeText(RegisterActivity.this, "Please select your profile", Toast.LENGTH_SHORT).show();
                } else if (!matcher.matches()) {
                    Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                }else {
                    registerUser();
                }
            }
        });

    }

    private void registerUser() {
        sign_up_btn.setEnabled(false);
        sign_up_btn.setText("");
        login_progress.setVisibility(View.VISIBLE);
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/register";

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        JSONObject result = null;
                        try {
                            result = new JSONObject(resultResponse);
                            String status = result.getString("status");
                            String message = result.getString("message");
                            if (status.equals("200")) {
                                selectedImageUri = null;
                                name_editText.setText("");
                                email_editText.setText("");
                                pass_editText.setText("");
                                // tell everybody you have succeed upload image and post strings
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                                login_progress.setVisibility(View.GONE);
                                sign_up_btn.setText("Sign up");
                                sign_up_btn.setEnabled(true);
                            } else {
                                Log.i("Unexpected", message);
                                login_progress.setVisibility(View.GONE);
                                sign_up_btn.setText("Sign up");
                                sign_up_btn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            login_progress.setVisibility(View.GONE);
                            sign_up_btn.setText("Sign up");
                            sign_up_btn.setEnabled(true);
                            Log.e(TAG, "Error parsing JSON response: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = "Unknown error";
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = "Request timeout";
                                login_progress.setVisibility(View.GONE);
                                sign_up_btn.setText("Sign up");
                                sign_up_btn.setEnabled(true);
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = "Failed to connect server";
                                login_progress.setVisibility(View.GONE);
                                sign_up_btn.setText("Sign up");
                                sign_up_btn.setEnabled(true);
                            }
                        } else {
                            String result = null;
                            try {
                                result = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                                Log.d("Error : ", result);
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException(e);
                            }
                            Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                Log.e("Error Status", status);
                                Log.e("Error Message", message);

                                if (networkResponse.statusCode == 404) {
                                    errorMessage = "Resource not found";
                                    login_progress.setVisibility(View.GONE);
                                    sign_up_btn.setText("Sign up");
                                    sign_up_btn.setEnabled(true);
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message+" Unauthorized";
                                    login_progress.setVisibility(View.GONE);
                                    sign_up_btn.setText("Sign up");
                                    sign_up_btn.setEnabled(true);
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message+ "Bad request";
                                    login_progress.setVisibility(View.GONE);
                                    sign_up_btn.setText("Sign up");
                                    sign_up_btn.setEnabled(true);
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message+" Something is getting wrong";
                                    login_progress.setVisibility(View.GONE);
                                    sign_up_btn.setText("Sign up");
                                    sign_up_btn.setEnabled(true);
                                }
                            } catch (JSONException e) {
                                login_progress.setVisibility(View.GONE);
                                sign_up_btn.setText("Sign up");
                                sign_up_btn.setEnabled(true);
                                Log.e(TAG, "Error parsing JSON response: ", e);
                            }
                        }
                        Log.i("Error", errorMessage);
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        login_progress.setVisibility(View.GONE);
                        sign_up_btn.setText("Sign up");
                        sign_up_btn.setEnabled(true);
                        Log.e(TAG, "Error parsing JSON response: ", error);
                    }
                }) {
            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();
                params.put("profilePicture", new DataPart(fileName, multiMediaByteArray, fileType));
                return params;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> text = new HashMap<>();
                text.put("userName", name);
                text.put("email", email);
                text.put("password", password);
                return text;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);

        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT
        ));
    }

    /**
     * Handle image selection from file storage
     */
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Add extra MIME types for better compatibility
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg", "image/gif", "image/webp", "image/bmp", "image/tiff"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Allow multiple file managers and gallery apps
        Intent chooser = Intent.createChooser(intent, "Select Image");

        try {
            imagePickerLauncher.launch(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Error launching image picker", e);
            Toast.makeText(getApplicationContext(), "Unable to open file picker", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the display name of a file from its URI
     * @param uri The file URI
     * @return The file name or a default name if unable to retrieve
     */
    private String getFileName(Uri uri) {
        String fileName = "Unknown file";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get file name from URI", e);
            // Fallback: try to get filename from URI path
            String path = uri.getLastPathSegment();
            if (path != null && path.contains("/")) {
                fileName = path.substring(path.lastIndexOf("/") + 1);
            }
        }

        return fileName != null ? fileName : "Unknown file";
    }

    private String getFileType(Uri uri) {
        String fileType = "application/octet-stream"; // default binary stream type

        try {
            // Try to get the MIME type from the content resolver
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            String type = contentResolver.getType(uri);
            if (type != null) {
                fileType = type;
            } else {
                // Fallback: Try to guess from file extension
                String fileName = getFileName(uri);
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
                if (extension != null) {
                    String guessedType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    if (guessedType != null) {
                        fileType = guessedType;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to get file type from URI", e);
        }

        return fileType;
    }

}
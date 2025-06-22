package com.example.wellbeing;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.UtilsServices.UriToByteArrayConverterUtil;
import com.example.wellbeing.UtilsServices.VolleyMultipartRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateAccountDetails extends AppCompatActivity {
    private static final String TAG = "UpdateAccountDetails";
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    // Increase these values significantly for file uploads
    private static final int TIMEOUT_MS = 60000; // 60 seconds (was probably 30 seconds)
    private static final int MAX_RETRIES = 1; // Reduce retries to avoid multiple uploads
    private static final float BACKOFF_MULT = 1.0f;
    SharedPreferenceClass sharedPreferenceClass;
    String accessToken;
    String fileName, fileType, name, intentName, intentProfile;
    byte[] multiMediaByteArray;
    CircleImageView circleImageView;
    ImageView plusIconIV;
    Button updateBtn;
    EditText userName;
    Uri selectedImageUri;
    ProgressBar updateProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_account_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        circleImageView = findViewById(R.id.circleImageView);
        plusIconIV = findViewById(R.id.plusIconIV);
        updateBtn = findViewById(R.id.updateBtn);
        userName = findViewById(R.id.userName);
        updateProgress = findViewById(R.id.updateProgress);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");

        // Get the Intent that started this activity
        Intent intent = getIntent();

        // Extract the data
        intentName = intent.getStringExtra("userName");
        intentProfile = intent.getStringExtra("profilePic");

        userName.setText(intentName);
        Picasso.get().load(intentProfile).into(circleImageView);

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Objects.equals(intentName, userName.getText().toString()) || selectedImageUri != null) {
                    updateAccountDetails();
                }else {
                    Toast.makeText(UpdateAccountDetails.this, "No change", Toast.LENGTH_SHORT).show();
                }
            }
        });

        plusIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

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
    }

    private void updateAccountDetails() {
        updateBtn.setEnabled(false);
        updateBtn.setText("");
        updateProgress.setVisibility(View.VISIBLE);
        String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/users/update-account";

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        updateProgress.setVisibility(View.GONE);
                        updateBtn.setText("Update");
                        updateBtn.setEnabled(true);
                        String resultResponse = new String(response.data);
                        JSONObject result = null;
                        try {
                            result = new JSONObject(resultResponse);
                            String status = result.getString("status");
                            String message = result.getString("message");
                            JSONObject data = result.getJSONObject("data");
                            if (status.equals("200")) {
                                Toast.makeText(UpdateAccountDetails.this, message, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent();
                                intent.putExtra("userName", data.getString("userName"));
                                intent.putExtra("profilePic", data.getString("profilePicture"));
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                Log.i("Unexpected", message);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateProgress.setVisibility(View.GONE);
                        updateBtn.setText("Update");
                        updateBtn.setEnabled(true);
                        String errorMessage = "Unknown error";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                // Optionally parse the JSON to extract a specific message
                                JSONObject data = new JSONObject(responseBody);
                                if (data.has("message")) {
                                    errorMessage = data.getString("message");
                                } else {
                                    errorMessage = responseBody;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            errorMessage = error.toString();
                            Log.e(TAG, "Error parsing JSON response: ", error);
                        }
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }

                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws AuthFailureError {
                Map<String, DataPart> params = new HashMap<>();

                // Only add profile picture if we have valid data
                if (multiMediaByteArray != null && multiMediaByteArray.length > 0 &&
                        fileName != null && !fileName.isEmpty() &&
                        fileType != null && !fileType.isEmpty()) {
                    params.put("profilePicture", new DataPart(fileName, multiMediaByteArray, fileType));
                }

                return params;
            }

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> text = new HashMap<>();
                text.put("userName", userName.getText().toString());
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
}
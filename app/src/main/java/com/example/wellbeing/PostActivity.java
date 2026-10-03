

package com.example.wellbeing;

import com.example.wellbeing.UtilsServices.ApiClient;
import com.example.wellbeing.UtilsServices.MultipartUploader;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";
    EditText descriptionET;
    MaterialButton send;
    String task, description, accessToken;
    String videoDuration = "0"; // Added video duration variable
    SharedPreferenceClass sharedPreferenceClass;
    ProgressDialog progressDialog;

    // UI Components
    private MaterialButton btnPickImage;
    private MaterialButton btnPickVideo;
    private MaterialCardView cardSelectedFile;
    private ImageView ivFileType;
    private TextView tvFileName;
    private ImageButton btnCancelFile;

    // File handling
    private Uri selectedFileUri;
    private String selectedFileName;
    private boolean isImageSelected = false;
    private boolean isVideoSelected = false;
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        descriptionET = findViewById(R.id.description_et);
        send = findViewById(R.id.upload_post);
        sharedPreferenceClass = new SharedPreferenceClass(this);
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        task = sharedPreferenceClass.getValue_string("taskId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Upload Post");
        progressDialog.setMessage("Post is uploading");

        btnPickImage = findViewById(R.id.btn_pick_image);
        btnPickVideo = findViewById(R.id.btn_pick_video);
        cardSelectedFile = findViewById(R.id.card_selected_file);
        ivFileType = findViewById(R.id.iv_file_type);
        tvFileName = findViewById(R.id.tv_file_name);
        btnCancelFile = findViewById(R.id.btn_cancel_file);

        setupActivityResultLaunchers();
        setupClickListeners();
    }

    private void setupActivityResultLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleSelectedFile(imageUri, true);
                        }
                    }
                }
        );

        // Video picker launcher
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            handleSelectedFile(videoUri, false);
                        }
                    }
                }
        );
    }

    public void uploadPost() {
        description = descriptionET.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(PostActivity.this, "Description is required", Toast.LENGTH_SHORT).show();
        } else if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
        } else if (MultipartUploader.getFileSize(this, selectedFileUri) > MultipartUploader.MAX_FILE_SIZE) {
            Toast.makeText(this, "File is too large. Maximum size is 100 MB", Toast.LENGTH_LONG).show();
        } else {
            progressDialog.show();
            send.setEnabled(false);

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("task", task);
            fields.put("description", description);
            fields.put("mediaType", isImageSelected ? "image" : "video");
            fields.put("duration", videoDuration);

            MultipartUploader.upload(this, ApiClient.BASE_URL + "/upload/upload-post", accessToken, fields,
                    "multiMedia", selectedFileUri, selectedFileName, new MultipartUploader.Callback() {
                        @Override
                        public void onSuccess(String responseBody) {
                            if (isFinishing() || isDestroyed()) return;
                            progressDialog.dismiss();
                            send.setEnabled(true);
                            String message = "Post uploaded successfully";
                            try {
                                message = new JSONObject(responseBody).optString("message", message);
                            } catch (JSONException e) {
                                Log.e(TAG, "uploadPost error : ", e);
                            }
                            Toast.makeText(PostActivity.this, message, Toast.LENGTH_SHORT).show();
                            sharedPreferenceClass.setValue_string("statusFlag", "completed");
                            startActivity(new Intent(PostActivity.this, HomeActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(int statusCode, String message) {
                            if (isFinishing() || isDestroyed()) return;
                            progressDialog.dismiss();
                            send.setEnabled(true);
                            ApiClient.showError(PostActivity.this, statusCode, message);
                        }
                    });
        }
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        btnPickImage.setOnClickListener(v -> pickImage());
        btnPickVideo.setOnClickListener(v -> pickVideo());
        btnCancelFile.setOnClickListener(v -> clearSelectedFile());
        send.setOnClickListener(v -> uploadPost());
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
            Toast.makeText(this, "Unable to open file picker", Toast.LENGTH_SHORT).show();

        }
    }

    /**
     * Handle video selection from file storage
     */
    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Add extra MIME types for better compatibility
        String[] mimeTypes = {"video/mp4", "video/avi", "video/mov", "video/wmv", "video/3gp", "video/mkv", "video/flv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        // Allow multiple file managers and gallery apps
        Intent chooser = Intent.createChooser(intent, "Select Video");

        try {
            videoPickerLauncher.launch(chooser);
        } catch (Exception e) {
            Log.e(TAG, "Error launching video picker", e);
            Toast.makeText(this, "Unable to open file picker", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the selected file URI and update UI
     * @param fileUri The selected file URI
     * @param isImage True if the selected file is an image, false for video
     */
    private void handleSelectedFile(Uri fileUri, boolean isImage) {
        try {
            selectedFileUri = fileUri;
            isImageSelected = isImage;
            isVideoSelected = !isImage;

            // Get file name
            selectedFileName = getFileName(fileUri);

            // Get video duration if it's a video file
            if (isVideoSelected) {
                videoDuration = getVideoDuration(fileUri);
                Log.d(TAG, "Video duration: " + videoDuration + " seconds");
            } else {
                videoDuration = "0"; // Reset duration for images
            }

            // Update UI
            updateSelectedFileUI(fileUri);

            Log.d(TAG, "File selected: " + selectedFileName + " (Type: " +
                    (isImage ? "Image" : "Video") + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error handling selected file", e);
            Toast.makeText(this, "Error processing selected file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get video duration in seconds
     * @param videoUri The video file URI
     * @return Duration in seconds as a string, "0" if unable to retrieve
     */
    private String getVideoDuration(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                long durationMs = Long.parseLong(durationStr);
                long durationSeconds = durationMs / 1000;
                return String.valueOf(durationSeconds);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting video duration", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        return "0";
    }

    /**
     * Get the display name of a file from its URI
     * @param uri The file URI
     * @return The file name or a default name if unable to retrieve
     */
    private String getFileName(Uri uri) {
        String fileName = "Unknown file";

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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

    /**
     * Update UI to show the selected file information
     */
    private void updateSelectedFileUI(Uri fileUri) {
        // Show the selected file card
        cardSelectedFile.setVisibility(View.VISIBLE);

        // Set file name
        tvFileName.setText(selectedFileName);

        // Set appropriate icon based on file type
        ivFileType.setImageResource(isImageSelected ? R.drawable.ic_image : R.drawable.ic_video);

        // Enable upload button
        send.setEnabled(true);
    }

    /**
     * Clear the selected file and reset UI state
     */
    private void clearSelectedFile() {
        selectedFileUri = null;
        selectedFileName = null;
        isImageSelected = false;
        isVideoSelected = false;
        videoDuration = "0"; // Reset video duration

        // Hide the selected file card
        cardSelectedFile.setVisibility(View.GONE);

        Log.d(TAG, "Selected file cleared");
    }
}
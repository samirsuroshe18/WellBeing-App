package com.example.wellbeing.fragments;

import com.example.wellbeing.UtilsServices.ApiClient;
import com.example.wellbeing.UtilsServices.MultipartUploader;
import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.wellbeing.R;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class CreateFragment extends Fragment {
    private static final String TAG = "CreateFragment";
    EditText taskTitle, taskDescription, taskTime;
    MaterialButton send;
    String title, description, time, mediaType, accessToken;
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

    public CreateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        send = view.findViewById(R.id.upload_post);
        taskTitle = view.findViewById(R.id.taskTitle);
        taskDescription = view.findViewById(R.id.taskDescription);
        taskTime = view.findViewById(R.id.taskTime);
        sharedPreferenceClass = new SharedPreferenceClass(requireContext());
        accessToken = sharedPreferenceClass.getValue_string("accessToken");
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Create Task");
        progressDialog.setMessage("Task is uploading");

        btnPickImage = view.findViewById(R.id.btn_pick_image);
        btnPickVideo = view.findViewById(R.id.btn_pick_video);
        cardSelectedFile = view.findViewById(R.id.card_selected_file);
        ivFileType = view.findViewById(R.id.iv_file_type);
        tvFileName = view.findViewById(R.id.tv_file_name);
        btnCancelFile = view.findViewById(R.id.btn_cancel_file);

        setupActivityResultLaunchers();
        setupClickListeners();

        return view;
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
     * Handle the upload post action
     */
    private void uploadPost() {
        title = taskTitle.getText().toString().trim();
        description = taskDescription.getText().toString().trim();
        time = taskTime.getText().toString().trim();
        if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (selectedFileUri == null) {
            Toast.makeText(getContext(), "Please select a file first", Toast.LENGTH_SHORT).show();
        } else if (MultipartUploader.getFileSize(requireContext(), selectedFileUri) > MultipartUploader.MAX_FILE_SIZE) {
            Toast.makeText(getContext(), "File is too large. Maximum size is 100 MB", Toast.LENGTH_LONG).show();
        } else {
            progressDialog.show();
            send.setEnabled(false);

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("title", title);
            fields.put("description", description);
            fields.put("timeToComplete", time);
            fields.put("mediaType", isImageSelected ? "image" : "video");

            MultipartUploader.upload(requireContext(), ApiClient.BASE_URL + "/tasklist/create-task", accessToken, fields,
                    "taskReference", selectedFileUri, selectedFileName, new MultipartUploader.Callback() {
                        @Override
                        public void onSuccess(String responseBody) {
                            if (!isAdded()) return;
                            progressDialog.dismiss();
                            String message = "Task is created successfully";
                            try {
                                message = new JSONObject(responseBody).optString("message", message);
                            } catch (JSONException e) {
                                Log.e(TAG, "uploadPost error : ", e);
                            }
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            taskTitle.setText("");
                            taskDescription.setText("");
                            taskTime.setText("");
                            clearSelectedFile();
                        }

                        @Override
                        public void onError(int statusCode, String message) {
                            if (!isAdded()) return;
                            progressDialog.dismiss();
                            send.setEnabled(true);
                            ApiClient.showError(getContext(), statusCode, message);
                        }
                    });
        }
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
            Toast.makeText(getContext(), "Unable to open file picker", Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getContext(), "Unable to open file picker", Toast.LENGTH_SHORT).show();
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

            // Update UI
            updateSelectedFileUI(fileUri);

            Log.d(TAG, "File selected: " + selectedFileName + " (Type: " +
                    (isImage ? "Image" : "Video") + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error handling selected file", e);
            Toast.makeText(getContext(), "Error processing selected file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the display name of a file from its URI
     * @param uri The file URI
     * @return The file name or a default name if unable to retrieve
     */
    private String getFileName(Uri uri) {
        String fileName = "Unknown file";

        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
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

        // Hide the selected file card
        cardSelectedFile.setVisibility(View.GONE);

        // Disable upload button
        send.setEnabled(false);

        Log.d(TAG, "Selected file cleared");
    }
}
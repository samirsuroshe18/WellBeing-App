package com.example.wellbeing.fragments;

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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.wellbeing.R;
import com.example.wellbeing.UtilsServices.SharedPreferenceClass;
import com.example.wellbeing.UtilsServices.UriToByteArrayConverterUtil;
import com.example.wellbeing.UtilsServices.VolleyMultipartRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CreateFragment extends Fragment {
    private static final String TAG = "CreateFragment";
    public static final int TIMEOUT_MS = 10000;
    public static final int MAX_RETRIES = 2;
    public static final float BACKOFF_MULT = 2.0f;
    byte[] multiMediaByteArray;
    EditText taskTitle, taskDescription, taskTime;
    MaterialButton send;
    String title, description, time, mediaType, accessToken, fileName;
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
        title = taskTitle.getText().toString();
        description = taskDescription.getText().toString();
        time = taskTime.getText().toString();
        if (title.isEmpty() || description.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (isImageSelected && isVideoSelected) {
            Toast.makeText(getContext(), "Please provide media reference", Toast.LENGTH_SHORT).show();
        } else if (selectedFileUri == null) {
            Toast.makeText(getContext(), "Please select a file first", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.show();
            String apiKey = "https://wellbeing-backend-5f8e.onrender.com/api/v1/tasklist/create-task";
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiKey,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            String resultResponse = new String(response.data, StandardCharsets.UTF_8);
                            try {
                                JSONObject result = new JSONObject(resultResponse);
                                String status = result.getString("status");
                                String message = result.getString("message");

                                if (status.equals("200")) {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                    taskTitle.setText("");
                                    taskDescription.setText("");
                                    taskTime.setText("");
                                    clearSelectedFile();
                                    progressDialog.dismiss();
                                } else {
                                    Log.i("Unexpected", message);
                                    progressDialog.dismiss();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "uploadPost error : ", e);
                                progressDialog.dismiss();
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
                                    progressDialog.dismiss();
                                } else if (error.getClass().equals(NoConnectionError.class)) {
                                    errorMessage = "Failed to connect server";
                                    progressDialog.dismiss();
                                }
                            } else {
                                String result = new String(networkResponse.data, StandardCharsets.UTF_8);
                                try {
                                    JSONObject response = new JSONObject(result);
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    Log.e("Error Status", status);
                                    Log.e("Error Message", message);

                                    if (networkResponse.statusCode == 404) {
                                        errorMessage = "Resource not found";
                                        progressDialog.dismiss();
                                    } else if (networkResponse.statusCode == 401) {
                                        errorMessage = message+" Please login again";
                                        progressDialog.dismiss();
                                    } else if (networkResponse.statusCode == 400) {
                                        errorMessage = message+ " Check your inputs";
                                        progressDialog.dismiss();
                                    } else if (networkResponse.statusCode == 500) {
                                        errorMessage = message+" Something is getting wrong";
                                        progressDialog.dismiss();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "uploadPost error : ", e);
                                    progressDialog.dismiss();
                                }
                            }
                            Log.i("Error", errorMessage);
                            Log.e(TAG, "uploadPost error : ", error);
                            progressDialog.dismiss();
                        }
                    })
            {
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("taskReference", new DataPart(fileName, multiMediaByteArray));
                    return params;
                }

                @NonNull
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> text = new HashMap<>();
                    text.put("title", title);
                    text.put("description", description);
                    text.put("timeToComplete", time);
                    text.put("mediaType", isImageSelected ? "image" : "video");
                    return text;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer "+accessToken);
                    return headers;
                }
            };

            //adding the request to volley
            Volley.newRequestQueue(requireContext()).add(volleyMultipartRequest);

            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    TIMEOUT_MS,
                    MAX_RETRIES,
                    BACKOFF_MULT
            ));

            volleyMultipartRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

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
    private void updateSelectedFileUI(Uri fileUri) throws IOException {
        try {
            // Show the selected file card
            cardSelectedFile.setVisibility(View.VISIBLE);

            // Set file name
            tvFileName.setText(selectedFileName);

            // Set appropriate icon based on file type
            if (isImageSelected) {
                ivFileType.setImageResource(R.drawable.ic_image);
                multiMediaByteArray = UriToByteArrayConverterUtil.convertUriToByteArray(requireContext(), fileUri);
            } else if (isVideoSelected) {
                ivFileType.setImageResource(R.drawable.ic_video);
                multiMediaByteArray = UriToByteArrayConverterUtil.convertUriToByteArray(requireContext(), fileUri);
            }

            // Enable upload button
            send.setEnabled(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
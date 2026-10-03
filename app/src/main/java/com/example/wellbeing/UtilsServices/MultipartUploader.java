package com.example.wellbeing.UtilsServices;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Uploads a file picked by the user as multipart/form-data.
 * The file is streamed from its Uri, so large videos are never held in memory.
 */
public final class MultipartUploader {
    public static final long MAX_FILE_SIZE = 100L * 1024 * 1024;

    private static final String LINE_END = "\r\n";
    private static final int CONNECT_TIMEOUT_MS = 60000;
    private static final int READ_TIMEOUT_MS = 300000;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String responseBody);

        void onError(int statusCode, String message);
    }

    private MultipartUploader() {
    }

    public static void upload(Context context, String url, String accessToken, Map<String, String> fields,
                              String fileField, Uri fileUri, String fileName, Callback callback) {
        Context appContext = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String boundary = "apiclient-" + System.currentTimeMillis();
                String mimeType = appContext.getContentResolver().getType(fileUri);
                long fileSize = getFileSize(appContext, fileUri);

                ByteArrayOutputStream head = new ByteArrayOutputStream();
                for (Map.Entry<String, String> field : fields.entrySet()) {
                    write(head, "--" + boundary + LINE_END);
                    write(head, "Content-Disposition: form-data; name=\"" + field.getKey() + "\"" + LINE_END);
                    write(head, "Content-Type: text/plain; charset=UTF-8" + LINE_END + LINE_END);
                    write(head, field.getValue() + LINE_END);
                }
                write(head, "--" + boundary + LINE_END);
                write(head, "Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\""
                        + safeFileName(fileName) + "\"" + LINE_END);
                write(head, "Content-Type: " + (mimeType != null ? mimeType : "application/octet-stream")
                        + LINE_END + LINE_END);
                byte[] headBytes = head.toByteArray();
                byte[] tailBytes = (LINE_END + "--" + boundary + "--" + LINE_END).getBytes(StandardCharsets.UTF_8);

                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                if (fileSize >= 0) {
                    connection.setFixedLengthStreamingMode(headBytes.length + fileSize + tailBytes.length);
                } else {
                    connection.setChunkedStreamingMode(0);
                }

                try (OutputStream output = connection.getOutputStream();
                     InputStream input = appContext.getContentResolver().openInputStream(fileUri)) {
                    if (input == null) {
                        throw new IOException("Unable to open the selected file");
                    }
                    output.write(headBytes);
                    byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.write(tailBytes);
                }

                int statusCode = connection.getResponseCode();
                InputStream responseStream = statusCode < 400 ? connection.getInputStream() : connection.getErrorStream();
                byte[] body = responseStream != null ? readAll(responseStream) : new byte[0];

                if (statusCode < 400) {
                    String responseBody = new String(body, StandardCharsets.UTF_8);
                    MAIN_THREAD.post(() -> callback.onSuccess(responseBody));
                } else {
                    String message = ApiClient.errorMessage(statusCode, body);
                    MAIN_THREAD.post(() -> callback.onError(statusCode, message));
                }
            } catch (SocketTimeoutException e) {
                MAIN_THREAD.post(() -> callback.onError(0, "Server is taking too long to respond. Please try again"));
            } catch (Exception e) {
                MAIN_THREAD.post(() -> callback.onError(0, "Upload failed. Check your internet connection"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * @return the size in bytes, or -1 when the provider does not report it
     */
    public static long getFileSize(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {
            // size stays unknown
        }
        return -1;
    }

    private static String safeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "upload";
        }
        return fileName.replace("\"", "").replace("\r", "").replace("\n", "");
    }

    private static void write(OutputStream output, String text) throws IOException {
        output.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] readAll(InputStream input) throws IOException {
        try (InputStream in = input) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8 * 1024];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        }
    }
}

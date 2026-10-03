package com.example.wellbeing.UtilsServices;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.example.wellbeing.LoginActivity;

import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class ApiClient {
    public static final String BASE_URL = "https://wellbeing-backend-5f8e.onrender.com/api/v1";

    // The server can take close to a minute to wake up after being idle
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int WRITE_TIMEOUT_MS = 60000;
    private static final int UPLOAD_TIMEOUT_MS = 180000;

    private static RequestQueue requestQueue;

    private ApiClient() {
    }

    public static synchronized RequestQueue getQueue(Context context) {
        if (requestQueue == null) {
            File cacheDir = new File(context.getApplicationContext().getCacheDir(), "volley");
            requestQueue = new RequestQueue(new DiskBasedCache(cacheDir), new BasicNetwork(new HurlStack())) {
                @Override
                public <T> Request<T> add(Request<T> request) {
                    request.setRetryPolicy(retryPolicyFor(request));
                    return super.add(request);
                }
            };
            requestQueue.start();
        }
        return requestQueue;
    }

    // Only GET requests are safe to send again; a repeated POST would create duplicates
    private static RetryPolicy retryPolicyFor(Request<?> request) {
        if (request.getMethod() == Request.Method.GET) {
            return new DefaultRetryPolicy(READ_TIMEOUT_MS, 1, 1f);
        }
        if (request instanceof VolleyMultipartRequest) {
            return new DefaultRetryPolicy(UPLOAD_TIMEOUT_MS, 0, 1f);
        }
        return new DefaultRetryPolicy(WRITE_TIMEOUT_MS, 0, 1f);
    }

    public static String errorMessage(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse == null) {
            if (error instanceof TimeoutError) {
                return "Server is taking too long to respond. Please try again";
            }
            if (error instanceof NoConnectionError || error instanceof NetworkError) {
                return "Failed to connect to the server. Check your internet connection";
            }
            return "Something went wrong. Please try again";
        }
        return errorMessage(networkResponse.statusCode, networkResponse.data);
    }

    public static String errorMessage(int statusCode, byte[] body) {
        if (body != null) {
            try {
                JSONObject response = new JSONObject(new String(body, StandardCharsets.UTF_8));
                String message = response.optString("message");
                if (!message.isEmpty()) {
                    return message;
                }
            } catch (Exception ignored) {
                // the body was not json, fall through to the generic message
            }
        }
        return "Something went wrong (" + statusCode + ")";
    }

    public static void showError(Context context, VolleyError error) {
        int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : 0;
        showError(context, statusCode, errorMessage(error));
    }

    public static void showError(Context context, int statusCode, String message) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        SharedPreferenceClass preferences = new SharedPreferenceClass(appContext);
        boolean loggedIn = !preferences.getValue_string("accessToken").isEmpty();

        if (statusCode == 401 && loggedIn) {
            preferences.clearSession();
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
            message = "Session expired. Please login again";
        }
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
    }
}

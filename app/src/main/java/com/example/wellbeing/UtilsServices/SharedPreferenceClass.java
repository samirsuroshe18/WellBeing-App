package com.example.wellbeing.UtilsServices;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferenceClass {
    private static final String USER_PREFERENCE = "user_wellBeing";
    private static final String USER_ID = "userId";
    // Task progress belongs to an account, so these keys are stored per logged in user
    private static final Set<String> USER_SCOPED_KEYS = new HashSet<>(
            Arrays.asList("acceptFlag", "statusFlag", "taskId", "acceptedTaskId", "List"));
    SharedPreferences appShared;
    SharedPreferences.Editor prefsEditor;

    public SharedPreferenceClass(Context context){
        appShared = context.getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE);
        this.prefsEditor = appShared.edit();
    }

    public String getValue_string(String key){
        return appShared.getString(scopedKey(key), "");
    }

    public void setValue_string(String key, String value){
        prefsEditor.putString(scopedKey(key), value).commit();
    }

    public void remove(String key){
        prefsEditor.remove(scopedKey(key)).apply();
    }

    /**
     * Remembers who is logged in. Task progress saved before accounts were tracked is moved to this user.
     */
    public void setUser(String userId){
        prefsEditor.putString(USER_ID, userId);
        for (String key : USER_SCOPED_KEYS) {
            String scoped = key + "_" + userId;
            if (appShared.contains(key)) {
                if (!appShared.contains(scoped)) {
                    prefsEditor.putString(scoped, appShared.getString(key, ""));
                }
                prefsEditor.remove(key);
            }
        }
        prefsEditor.commit();
    }

    public void clearSession(){
        prefsEditor.remove("accessToken").remove("refreshToken").remove(USER_ID).commit();
    }

    private String scopedKey(String key){
        String userId = appShared.getString(USER_ID, "");
        if (USER_SCOPED_KEYS.contains(key) && !userId.isEmpty()) {
            return key + "_" + userId;
        }
        return key;
    }
}

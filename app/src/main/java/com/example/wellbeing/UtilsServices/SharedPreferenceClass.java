package com.example.wellbeing.UtilsServices;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    private static final String USER_PREFERENCE = "user_wellBeing";
    SharedPreferences appShared;
    SharedPreferences.Editor prefsEditor;

    public SharedPreferenceClass(Context context){
        appShared = context.getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE);
        this.prefsEditor = appShared.edit();
    }

    public String getValue_string(String key){
        return appShared.getString(key, "");
    }

    public void setValue_string(String key, String value){
        prefsEditor.putString(key, value).commit();
    }

    public void clear(){
        prefsEditor.clear().commit();
    }
}

package com.example.barcodeqrapp;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    private static final String PREFERENCE_NAME = "MyPreferences";
    private static final String KEY_DEFAULT_BROWSER = "defaultBrowser";

    public static void setDefaultBrowser(Context context, String browserPackage) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEFAULT_BROWSER, browserPackage);
        editor.apply();
    }

    public static String getDefaultBrowser(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_DEFAULT_BROWSER, null);
    }
}

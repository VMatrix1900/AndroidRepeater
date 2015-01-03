package com.example.vincent.repeater;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Vincent on 1/3/15.
 * static value for app status,
 * repeat mode, shuffle mode, AB repeat mode etc.
 * use sharedPreference to track persistent data.
 */
public class AppStatus extends Application {
    public static final String REPEAT_MODE = "Repeat_Mode";
    public static final String SHUFFLE_MODE = "Shuffle_Mode";
    public static final String ABREPEAT_MODE = "ABRepeat_Mode";

    public static final int REPEAT_OFF = 0;
    public static final int REPEAT_ON = 1;

    public static final int SHUFFLE_OFF = 0;
    public static final int SHUFFLE_ON = 1;

    public static final int ABREPEAT_OFF = 0;
    public static final int A_PRESSED = 1;
    public static final int B_PRESSED = 2;

    private static SharedPreferences status;

    @Override
    public void onCreate() {
        super.onCreate();
        status = getSharedPreferences(
                getString(R.string.app_status_pref_file_key), Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return status;
    }

    public int getABRepeatMode() {
        return status.getInt(ABREPEAT_MODE, ABREPEAT_OFF);
    }

}

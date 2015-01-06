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

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        status = getSharedPreferences(
                getString(R.string.app_status_pref_file_key), Context.MODE_PRIVATE);
        editor = ((AppStatus) getApplicationContext()).getSharedPreferences().edit();

    }

    public SharedPreferences getSharedPreferences() {
        return status;
    }

    public int getABRepeatMode() {
        return status.getInt(ABREPEAT_MODE, ABREPEAT_OFF);
    }

    public int getShuffleMode() {
        return status.getInt(SHUFFLE_MODE, SHUFFLE_OFF);
    }

    public int getRepeatMode() {
        return status.getInt(REPEAT_MODE, REPEAT_OFF);
    }

    public void toggleShuffleMode() {

        switch (getShuffleMode()) {
            case SHUFFLE_OFF:
                editor.putInt(SHUFFLE_MODE, SHUFFLE_ON);
                editor.commit();
                break;
            case SHUFFLE_ON:
                editor.putInt(SHUFFLE_MODE, SHUFFLE_OFF);
                editor.commit();
                break;
        }

    }

    public void toggleReapeatMode() {
        switch (getRepeatMode()) {
            case REPEAT_OFF:
                editor.putInt(REPEAT_MODE, REPEAT_ON);
                editor.commit();
                break;
            case REPEAT_ON:
                editor.putInt(REPEAT_MODE, REPEAT_OFF);
                editor.commit();
                break;
        }
    }

    public void toggleABReapeatMode() {
        switch (getABRepeatMode()) {
            case ABREPEAT_OFF:
                editor.putInt(ABREPEAT_MODE, A_PRESSED);
                editor.commit();
                break;
            case A_PRESSED:
                editor.putInt(ABREPEAT_MODE, B_PRESSED);
                editor.commit();
                break;
            case B_PRESSED:
                editor.putInt(ABREPEAT_MODE, ABREPEAT_OFF);
                editor.commit();
                break;
        }
    }
}

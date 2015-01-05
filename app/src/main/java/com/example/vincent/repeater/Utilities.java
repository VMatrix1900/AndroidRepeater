package com.example.vincent.repeater;

import java.util.concurrent.TimeUnit;

/**
 * Created by Vincent on 1/4/15.
 * helper class to transform progress to time format
 */
public class Utilities {
    /**
     * Function to convert milliseconds time to
     * Time Format
     * Minutes:Seconds
     */
    public static String milliSecondsToTimer(long milliseconds) {
        String timeString = "";
        String secondsString;
        String minutesString;

        // Convert total duration into time
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        timeString = timeString + minutesString + ":" + secondsString;

        // return time string
        return timeString;
    }
}

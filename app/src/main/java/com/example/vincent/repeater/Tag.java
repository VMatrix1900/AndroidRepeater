package com.example.vincent.repeater;

/**
 * Created by Vincent on 1/5/15.
 * model class for tag
 */
public class Tag {
    private int startTime;
    private int endTime;
    private String description;

    public Tag(int startTime, int endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }
}

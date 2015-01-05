package com.example.vincent.repeater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vincent on 1/5/15.
 * custom adapter for tag and tagsListview
 */
public class TagAdapter extends BaseAdapter {
    private ArrayList<Tag> tags;
    private LayoutInflater tagInf;

    public TagAdapter(Context c, ArrayList<Tag> tags) {
        this.tags = tags;
        this.tagInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return tags.size();
    }

    @Override
    public Object getItem(int position) {
        return tags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout tagLay = (LinearLayout) tagInf.inflate(
                R.layout.song, null);
        TextView tagView = (TextView) tagLay.findViewById(R.id.title);
        TextView startTimeView = (TextView) tagLay.findViewById(R.id.lastTime);
        TextView endTimeView = (TextView) tagLay.findViewById(R.id.totalTime);
        Tag currentTag = tags.get(position);

        tagView.setText(currentTag.getDescription());
        startTimeView.setText(Utilities.milliSecondsToTimer(currentTag.getStartTime()));
        endTimeView.setText(Utilities.milliSecondsToTimer(currentTag.getEndTime()));
        return tagLay;
    }
}

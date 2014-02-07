package com.mapzen.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mapzen.geo.DistanceFormatter;

public class DistanceView extends TextView {
    private int distance;

    public DistanceView(Context context) {
        super(context);
    }

    public DistanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DistanceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getDistance() {
        return distance;
    }

    public void setFormattedDistance(int distance, boolean realTime) {
        this.distance = distance;
        setText(DistanceFormatter.format(distance, realTime));
    }

    public void setFormattedDistance(int distance) {
        this.distance = distance;
        setText(DistanceFormatter.format(distance, false));
    }
}

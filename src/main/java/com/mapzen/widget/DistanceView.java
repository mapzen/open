package com.mapzen.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.mapzen.helpers.DistanceFormatter;

public class DistanceView extends TextView {
    private int distance;
    private boolean realTime = false;

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

    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public void setDistance(int distance) {
        this.distance = distance;
        setText(DistanceFormatter.format(distance, realTime));
    }

}

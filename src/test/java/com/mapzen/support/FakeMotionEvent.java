package com.mapzen.support;

import org.oscim.event.MotionEvent;

public class FakeMotionEvent extends MotionEvent {
    private int x;
    private int y;

    public FakeMotionEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public int getAction() {
        return 0;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getX(int idx) {
        return x;
    }

    @Override
    public float getY(int idx) {
        return y;
    }

    @Override
    public int getPointerCount() {
        return 0;
    }
}

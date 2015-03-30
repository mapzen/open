package org.oscim.map;

import org.oscim.android.AndroidMap;
import org.oscim.android.MapView;
import org.robolectric.Robolectric;

public class TestMap extends AndroidMap {
    private ViewController viewport;
    Animator mapAnimator;

    public TestMap() {
        super(new MapView(Robolectric.application));
        this.viewport = new TestViewport();
        mapAnimator = new Animator(this);
    }

    public void setViewport(ViewController viewport) {
        this.viewport = viewport;
    }

    @Override
    public void updateMap(boolean forceRedraw) {
    }

    @Override
    public void render() {
    }

    @Override
    public boolean post(Runnable action) {
        return false;
    }

    @Override
    public boolean postDelayed(Runnable action, long delay) {
        return false;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public Animator animator() {
        return mapAnimator;
    }

    public void setAnimator(Animator animator) {
        this.mapAnimator = animator;
    }

    @Override
    public ViewController viewport() {
        return viewport;
    }
}

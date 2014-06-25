package org.oscim.map;

public class TestMap extends Map {
    private TestViewport viewport;
    Animator mapAnimator;

    public TestMap() {
        super();
        this.viewport = new TestViewport();
        mapAnimator = new Animator(this);
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


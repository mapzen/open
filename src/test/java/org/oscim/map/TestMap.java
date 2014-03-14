package org.oscim.map;

public class TestMap extends Map {
    private TestViewport viewport;
    MapAnimator mapAnimator;

    public TestMap() {
        super();
        this.viewport = new TestViewport(this);
        mapAnimator = new MapAnimator(this, viewport);
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
    public MapAnimator animator() {
        return mapAnimator;
    }

    public void setAnimator(MapAnimator animator) {
        this.mapAnimator = animator;
    }

    @Override
    public TestViewport viewport() {
        return viewport;
    }
}

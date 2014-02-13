package org.oscim.map;

public class TestMap extends Map {
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
    public Viewport viewport() {
        return new Viewport(this);
    }
}

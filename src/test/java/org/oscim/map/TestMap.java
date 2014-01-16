package org.oscim.map;

import org.oscim.core.MapPosition;

public class TestMap extends Map {
    private TestViewport viewport;
    private MapPosition mapPosition;

    public TestMap() {
        super();
        this.viewport = new TestViewport(this);
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
    public void setMapPosition(double latitude, double longitude, double scale) {
        super.setMapPosition(latitude, longitude, scale);
        mapPosition = new MapPosition(latitude, longitude, scale);
    }

    @Override
    public MapPosition getMapPosition() {
        return mapPosition;
    }

    @Override
    public TestViewport viewport() {
        return viewport;
    }
}

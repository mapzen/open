package org.oscim.map;

public class TestViewport extends Viewport {
    private double rotation;

    TestViewport(Map map) {
        super(map);
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }
}

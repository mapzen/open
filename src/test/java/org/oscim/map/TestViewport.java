package org.oscim.map;

public class TestViewport extends ViewController {
    private double rotation, tilt;

    @Override
    public boolean setTilt(float tilt) {
        this.tilt = tilt;
        return true;
    }

    public double getTilt() {
        return tilt;
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }
}

package org.oscim.map;

public class TestViewport extends ViewController {
    private double rotation;

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }
}


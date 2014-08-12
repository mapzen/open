package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import android.location.Location;

public class RouteEngine {
    private Route route;
    private RouteListener listener;
    private ZoomController zoomController;
    private Instruction nextInstruction;
    private int currentIndex = -1;
    private int distance;

    public void onLocationChanged(Location location) {
        final Location snapLocation = route.snapToRoute(location);
        if (snapLocation == null) {
            if (route.isLost()) {
                listener.onRecalculate(location);
            }
            return;
        }

        listener.onSnapLocation(location, snapLocation);
        nextInstruction = route.getNextInstruction();
        if (nextInstruction == null) {
            return;
        }

        final int index = route.getRouteInstructions().indexOf(nextInstruction);
        distance = (int) Math.floor(snapLocation.distanceTo(nextInstruction.getLocation()));

        checkExitRadius(snapLocation);
        checkEnterRadius(index);
        listener.onUpdateDistance(distance);
    }

    private void checkExitRadius(Location snapLocation) {
        if (currentIndex > -1) {
            if (route.getRouteInstructions().get(currentIndex).getLocation()
                    .distanceTo(snapLocation) > zoomController.getTurnRadius()) {
                listener.onExitInstructionRadius(currentIndex);
                currentIndex = -1;
            }
        }
    }

    private void checkEnterRadius(int index) {
        if (distance < zoomController.getTurnRadius() &&
                !route.getSeenInstructions().contains(nextInstruction)) {
            listener.onEnterInstructionRadius(index);
            route.addSeenInstruction(nextInstruction);
            currentIndex = index;
        }
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setListener(RouteListener listener) {
        this.listener = listener;
    }

    public void setZoomController(ZoomController zoomController) {
        this.zoomController = zoomController;
    }

    public interface RouteListener {
        public void onRecalculate(Location location);
        public void onSnapLocation(Location originalLocation, Location snapLocation);
        public void onEnterInstructionRadius(int index);
        public void onExitInstructionRadius(int index);
        public void onUpdateDistance(int closestDistance);
    }
}

package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import android.location.Location;

public class RouteEngine {
    public static final int DESTINATION_RADIUS = 20;
    private Route route;
    private RouteListener listener;
    private Instruction nextInstruction;
    private int currentIndex = -1;
    private int closestDistance;
    private int distanceToDestination;
    private boolean routeComplete;

    public void onLocationChanged(Location location) {
        final Location snapLocation = route.snapToRoute(location);
        if (snapLocation == null) {
            if (route.isLost()) {
                listener.onRecalculate(location);
            }
            return;
        }

        listener.onSnapLocation(location, snapLocation);

        if (youHaveArrived(snapLocation)) {
            routeComplete = true;
            listener.onRouteComplete();
            return;
        }

        nextInstruction = route.getNextInstruction();
        if (nextInstruction == null) {
            return;
        }

        final int index = route.getRouteInstructions().indexOf(nextInstruction);
        closestDistance = (int) Math.floor(snapLocation.distanceTo(nextInstruction.getLocation()));
        checkExitRadius(snapLocation);
        checkEnterRadius(index);
        calculateDistance(snapLocation, index);
    }

    private boolean youHaveArrived(Location snapLocation) {
        return getDistanceToDestination(snapLocation) < DESTINATION_RADIUS && !routeComplete;
    }

    private float getDistanceToDestination(Location snapLocation) {
        final Location destination = getLocationForDestination();
        return snapLocation.distanceTo(destination);
    }

    private Location getLocationForDestination() {
        final int destinationIndex = route.getRouteInstructions().size() - 1;
        final Instruction destinationInstruction =
                route.getRouteInstructions().get(destinationIndex);
        return destinationInstruction.getLocation();
    }

    private void checkExitRadius(Location snapLocation) {
        if (currentIndex > -1) {
            if (route.getRouteInstructions().get(currentIndex).getLocation()
                    .distanceTo(snapLocation) > ZoomController.DEFAULT_TURN_RADIUS) {
                listener.onExitInstructionRadius(currentIndex);
                currentIndex = -1;
            }
        }
    }

    private void checkEnterRadius(int index) {
        if (closestDistance < ZoomController.DEFAULT_TURN_RADIUS &&
                !route.getSeenInstructions().contains(nextInstruction)) {

            if (index != route.getRouteInstructions().size() - 1) {
                listener.onEnterInstructionRadius(index);
            }

            calculateDistanceToDestination();
            route.addSeenInstruction(nextInstruction);
            currentIndex = index;
        }
    }

    private void calculateDistance(Location snapLocation, int index) {
        int instructionDistance = nextInstruction.getDistance();
        int currentDistanceToDestination = distanceToDestination;
        if (index > 0) {
            if (currentIndex > 0) {
                instructionDistance = route.getRouteInstructions().get(currentIndex - 1)
                        .getRemainingDistance(snapLocation);
            } else {
                instructionDistance = route.getRouteInstructions().get(index - 1)
                        .getRemainingDistance(snapLocation);
                currentDistanceToDestination -= route.getRouteInstructions().get(index - 1)
                        .getDistance();
                currentDistanceToDestination += instructionDistance;
            }
        }

        if (currentIndex == route.getRouteInstructions().size() - 1) {
            instructionDistance = 0;
            currentDistanceToDestination = 0;
        }

        listener.onUpdateDistance(closestDistance, instructionDistance,
                currentDistanceToDestination);
    }

    private void calculateDistanceToDestination() {
        distanceToDestination = route.getTotalDistance();
        for (Instruction instruction : route.getSeenInstructions()) {
            distanceToDestination -= instruction.getDistance();
        }
    }

    public void setRoute(Route route) {
        this.route = route;
        this.distanceToDestination = route.getTotalDistance();
    }

    public void setListener(RouteListener listener) {
        this.listener = listener;
    }

    public interface RouteListener {
        public void onRecalculate(Location location);
        public void onSnapLocation(Location originalLocation, Location snapLocation);
        public void onEnterInstructionRadius(int index);
        public void onExitInstructionRadius(int index);
        public void onUpdateDistance(int closestDistance, int instructionDistance,
                int distanceToDestination);
        public void onRouteComplete();
    }
}

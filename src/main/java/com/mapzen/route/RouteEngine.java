package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import android.location.Location;

import static com.mapzen.route.RouteEngine.RouteState.*;

public class RouteEngine {
    public static final int DESTINATION_RADIUS = 20;

    public enum RouteState {
        START,
        PRE_INSTRUCTION,
        INSTRUCTION,
        POST_INSTRUCTION,
        COMPLETE,
        LOST
    }

    private Route route;
    private RouteState routeState;
    private RouteListener listener;
    private int currentIndex;
    private int previousDistanceToNextInstruction;
    private Location location;
    private Location snapLocation;

    public void onLocationChanged(final Location location) {
        if (routeState == COMPLETE) {
            return;
        }

        if (routeState == LOST) {
            return;
        }

        this.location = location;
        snapLocation();

        if (routeState == START) {
            listener.onApproachInstruction(0);
            routeState=INSTRUCTION;
        }

        if (routeState == INSTRUCTION) {
            if (getDistanceToNextInstruction() > previousDistanceToNextInstruction) {
                routeState = POST_INSTRUCTION;
            }
        }

        if (routeState == POST_INSTRUCTION) {
            listener.onInstructionComplete(currentIndex);
            currentIndex++;
            routeState = PRE_INSTRUCTION;
        }

        if (routeState == PRE_INSTRUCTION) {
            if (getDistanceToNextInstruction() < ZoomController.DEFAULT_TURN_RADIUS &&
                    currentIndex != route.getRouteInstructions().size() - 1) {
                listener.onApproachInstruction(currentIndex);
                routeState = INSTRUCTION;
            }
        }

        previousDistanceToNextInstruction = getDistanceToNextInstruction();
    }

    private void snapLocation() {
        snapLocation = route.snapToRoute(location);

        if (snapLocation != null) {
            listener.onSnapLocation(location, snapLocation);
        }

        if (youHaveArrived()) {
            routeState = COMPLETE;
            listener.onRouteComplete();
        }

        if (route.isLost()) {
            routeState = LOST;
            listener.onRecalculate(location);
        }
    }

    private Instruction getNextInstruction() {
        return route.getRouteInstructions().get(currentIndex);
    }

    private int getDistanceToNextInstruction() {
        if (snapLocation == null) {
            return Integer.MAX_VALUE;
        }

        return (int) Math.floor(snapLocation.distanceTo(getNextInstruction().getLocation()));
    }

    private boolean youHaveArrived() {
        return snapLocation != null && getDistanceToDestination() < DESTINATION_RADIUS;
    }

    private float getDistanceToDestination() {
        final Location destination = getLocationForDestination();
        return snapLocation.distanceTo(destination);
    }

    private Location getLocationForDestination() {
        final int destinationIndex = route.getRouteInstructions().size() - 1;
        final Instruction destinationInstruction =
                route.getRouteInstructions().get(destinationIndex);
        return destinationInstruction.getLocation();
    }

    public void setRoute(Route route) {
        this.route = route;
        routeState = START;
        currentIndex = 0;
        previousDistanceToNextInstruction = 0;
    }

    public void setListener(RouteListener listener) {
        this.listener = listener;
    }

    public interface RouteListener {
        public void onRecalculate(Location location);
        public void onSnapLocation(Location originalLocation, Location snapLocation);
        public void onApproachInstruction(int index);
        public void onInstructionComplete(int index);
        public void onUpdateDistance(int closestDistance, int instructionDistance,
                int distanceToDestination);
        public void onRouteComplete();
    }
}

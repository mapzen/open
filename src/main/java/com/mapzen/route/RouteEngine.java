package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import android.location.Location;

import static com.mapzen.route.RouteEngine.RouteState.COMPLETE;
import static com.mapzen.route.RouteEngine.RouteState.INSTRUCTION;
import static com.mapzen.route.RouteEngine.RouteState.LOST;
import static com.mapzen.route.RouteEngine.RouteState.POST_INSTRUCTION;
import static com.mapzen.route.RouteEngine.RouteState.PRE_INSTRUCTION;
import static com.mapzen.route.RouteEngine.RouteState.START;

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
    private int nextIndex;
    private int previousDistanceToNextInstruction;
    private Location location;
    private Location snapLocation;

    public void onLocationChanged(final Location location) {
        if (routeState == COMPLETE) {
            return;
        }

        this.location = location;
        snapLocation();

        if (routeState == LOST) {
            return;
        }

        if (routeState == START) {
            listener.onApproachInstruction(0);
            routeState = INSTRUCTION;
        }

        if (routeState == INSTRUCTION) {
            if (getDistanceToNextInstruction() > previousDistanceToNextInstruction) {
                routeState = POST_INSTRUCTION;
            }
        }

        if (routeState == POST_INSTRUCTION) {
            listener.onInstructionComplete(nextIndex);
            nextIndex++;
            routeState = PRE_INSTRUCTION;
        }

        if (routeState == PRE_INSTRUCTION) {
            if (getDistanceToNextInstruction() < ZoomController.DEFAULT_TURN_RADIUS &&
                    nextIndex != route.getRouteInstructions().size() - 1) {
                listener.onApproachInstruction(nextIndex);
                routeState = INSTRUCTION;
            }
        }

        listener.onUpdateDistance(getDistanceToNextInstruction(), getDistanceToDestination());
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
        return route.getRouteInstructions().get(nextIndex);
    }

    private int getDistanceToNextInstruction() {
        if (snapLocation == null) {
            return Integer.MAX_VALUE;
        }

        return (int) Math.floor(snapLocation.distanceTo(getNextInstruction().getLocation()));
    }

    private boolean youHaveArrived() {
        return snapLocation != null && snapLocation.distanceTo(
                getLocationForDestination()) < DESTINATION_RADIUS;
    }

    private int getDistanceToDestination() {
        if (snapLocation == null) {
            return Integer.MAX_VALUE;
        }

        if (nextIndex == 0) {
            return route.getTotalDistance();
        }

        // Start with total route distance.
        float distanceToDestination = route.getTotalDistance();

        // Subtract distance for each instruction seen up to and including the current instruction.
        for (int i = 0; i < nextIndex; i++) {
            distanceToDestination -= route.getRouteInstructions().get(i).getDistance();
        }

        // Add remaining distance to the next instruction.
        distanceToDestination += getDistanceToNextInstruction();

        return (int) Math.floor(distanceToDestination);
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
        nextIndex = 0;
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
        public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination);
        public void onRouteComplete();
    }
}

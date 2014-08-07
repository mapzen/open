package com.mapzen.route;

import com.mapzen.helpers.ZoomController;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import android.location.Location;

import java.util.Iterator;

public class RouteEngine {
    private Route route;
    private RouteListener listener;
    private ZoomController zoomController;
    private int recordedDistance = 10000;
    private Instruction activeInstruction;

    public void onLocationChanged(Location location) {
        Location correctedLocation = route.snapToRoute(location);
        if (correctedLocation == null) {
            if (route.isLost()) {
                listener.onRecalculate(location);
            }
            return;
        }

        listener.onSnapLocation(location, correctedLocation);

        Instruction closestInstruction;
        int closestDistance;
        if (activeInstruction == null) {
            closestInstruction = route.getNextInstruction();
            if (closestInstruction == null) {
                return;
            }
            closestDistance =
                    (int) Math.floor(correctedLocation
                            .distanceTo(closestInstruction.getLocation()));
            activeInstruction = closestInstruction;
            recordedDistance = closestDistance;
        }

        closestDistance =
                (int) Math.floor(correctedLocation.distanceTo(activeInstruction.getLocation()));

        final int instructionIndex = route.getRouteInstructions().indexOf(activeInstruction);
        if (closestDistance < zoomController.getTurnRadius()) {
            listener.onNewInstruction(activeInstruction, instructionIndex);
            if (!route.getSeenInstructions().contains(activeInstruction)) {
                route.addSeenInstruction(activeInstruction);
            }
        }

        if (recordedDistance < closestDistance) {
            activeInstruction = null;
            recordedDistance = 100000;
        } else {
            recordedDistance = closestDistance;
        }

        final Iterator it = route.getSeenInstructions().iterator();
        while (it.hasNext()) {
            Instruction instruction = (Instruction) it.next();
            final Location l = new Location("temp");
            l.setLatitude(instruction.getLocation().getLatitude());
            l.setLongitude(instruction.getLocation().getLongitude());
            final int distance = (int) Math.floor(l.distanceTo(correctedLocation));
            if (distance > zoomController.getTurnRadius()) {
                listener.onFlipInstruction(instruction, correctedLocation);
            }
        }

        listener.onUpdateDistance(correctedLocation, closestDistance);
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
        public void onNewInstruction(Instruction instruction, int index);
        public void onFlipInstruction(Instruction instruction, Location snapLocation);
        public void onUpdateDistance(Location snapLocation, int closestDistance);
    }
}

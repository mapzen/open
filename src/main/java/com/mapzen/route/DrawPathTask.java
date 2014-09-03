package com.mapzen.route;

import com.mapzen.util.Logger;
import com.mapzen.util.RouteLocationIndicator;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.ViewController;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToGeoPoint;

public class DrawPathTask extends AsyncTask<ArrayList<Location>, Void, Void> {
    @Override
    protected Void doInBackground(ArrayList<Location>... locs) {
        final ArrayList<Location> locations = locs[0];
        final ViewController viewPort = getMapController().getMap().viewport();
        if (isCancelled()) {
            Logger.d("Cancelled before starting");
            return null;
        }
        BoundingBox boundingBox = viewPort.getBBox();
        getMapController().clearLines();

        long starttime = System.currentTimeMillis();
        PathLayer p = null;
        for (Location loc : locations) {
            if (isCancelled()) {
                Logger.d("Cancelled during iteration: index: "
                        + String.valueOf(locations.indexOf(loc)
                        + " of: " + String.valueOf(locations.size())));
                return null;
            }
            GeoPoint point = locationToGeoPoint(loc);
            if (boundingBox.contains(point)) {
                if (p == null) {
                    p = new PathLayer(
                            getMapController().getMap(), Color.BLACK, 8);
                    getMapController().getMap().layers().add(p);
                    getMapController().moveToTop(RouteLocationIndicator.class);
                }
                p.addPoint(point);
            } else {
                p = null;
            }
        }
        Logger.d("TIMING: " + (System.currentTimeMillis() - starttime));
        Logger.d("viewbox: " + viewPort.getBBox().toString());
        return null;
    }
}

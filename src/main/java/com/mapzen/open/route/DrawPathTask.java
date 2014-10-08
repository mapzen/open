package com.mapzen.open.route;

import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.util.Logger;
import com.mapzen.open.util.RouteLocationIndicator;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.ViewController;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.mapzen.open.MapController.locationToGeoPoint;

public class DrawPathTask extends AsyncTask<ArrayList<Location>, Void, Void> {
    @Inject MapController mapController;

    public DrawPathTask(MapzenApplication application) {
        application.inject(this);
    }

    @Override
    protected Void doInBackground(ArrayList<Location>... locs) {
        final ArrayList<Location> locations = locs[0];
        final ViewController viewPort = mapController.getMap().viewport();
        if (isCancelled()) {
            Logger.d("Cancelled before starting");
            return null;
        }
        BoundingBox boundingBox = viewPort.getBBox();

        ArrayList<PathLayer> layers = new ArrayList<PathLayer>();
        long starttime = System.currentTimeMillis();
        PathLayer p = null;
        boolean prePointAdded = false;
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
                            mapController.getMap(), Color.BLACK, 8);
                    layers.add(p);
                }
                if (!prePointAdded) {
                    int previousId = locations.indexOf(loc) - 1;
                    if (previousId > 0) {
                        p.addPoint(locationToGeoPoint(locations.get(previousId)));
                    }
                    prePointAdded = true;
                }
                p.addPoint(point);
            } else {
                prePointAdded = false;
                if (p != null) {
                    p.addPoint(locationToGeoPoint(loc));
                    p = null;
                }
            }
        }
        mapController.getMap().layers().addAll(layers);
        mapController.moveToTop(RouteLocationIndicator.class);
        mapController.clearLinesExcept(layers);
        Logger.d("TIMING: " + (System.currentTimeMillis() - starttime));
        Logger.d("viewbox: " + viewPort.getBBox().toString());
        return null;
    }
}

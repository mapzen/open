package com.mapzen.entity;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.MapzenApplication;
import com.mapzen.osrm.Route;
import com.mapzen.util.RouteLayer;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;

import java.util.ArrayList;

public class RouteInstruction extends Route {
    private String url;
    private RouteLayer layer;

    private String urlTemplate = "http://router.project-osrm.org/viaroute?z=%d"
            + "&output=json"
            + "&loc=%.6f,%.6f"
            + "&loc=%.6f,%.6f"
            + "&instructions=true";

    public RouteInstruction(ArrayList<GeoPoint> points, double zoomLevel) {
        // currently only support two points
        GeoPoint to = points.get(0);
        GeoPoint from = points.get(1);
        this.url = String.format(urlTemplate, (int) Math.floor(zoomLevel), from.getLatitude(),
                from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

    public void setLayer(RouteLayer layer) {
        this.layer = layer;
    }

    public void draw(Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                getSuccessListener(), getErrorListener());
        MapzenApplication.getApp(context).enqueueApiRequest(jsonObjectRequest);
    }


    private Response.ErrorListener getErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    private Response.Listener<JSONObject> getSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                layer.clear();
                setJsonObject(jsonObject);
                for(double[] pair : getGeometry()) {
                    layer.addPoint(new GeoPoint(pair[0], pair[1]));
                }
                layer.updateMap();
            }
        };
    }
}

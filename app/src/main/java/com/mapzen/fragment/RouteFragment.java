package com.mapzen.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.R;
import com.mapzen.adapters.RoutesAdapter;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.util.Logger;
import com.mapzen.util.MapzenProgressDialog;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;

import java.util.ArrayList;
import java.util.Locale;

public class RouteFragment extends BaseFragment {
    private ArrayList<Instruction> instructions;
    private GeoPoint from, destination;
    private String urlTemplate = "http://router.project-osrm.org/viaroute?z=%d"
            + "&output=json"
            + "&loc=%.6f,%.6f"
            + "&loc=%.6f,%.6f"
            + "&instructions=true";
    private ViewPager pager;
    private RoutesAdapter adapter;
    private Route route;

    public void setInstructions(ArrayList<Instruction> instructions) {
        Logger.d("instructions: " + instructions.toString());
        this.instructions = instructions;
    }

    @Override
    public void onResume() {
        super.onResume();
        act.hideActionBar();
        drawRoute();
    }

    @Override
    public void onPause() {
        super.onPause();
        clearRoute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        FrameLayout frame = (FrameLayout) container;
        frame.setVisibility(View.VISIBLE);

        pager = (ViewPager) rootView.findViewById(R.id.routes);
        adapter = new RoutesAdapter(getFragmentManager());
        adapter.setMap(mapFragment.getMap());
        adapter.setInstructions(instructions);
        adapter.setParent(this);
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.showActionBar();
        clearRoute();
    }

    public void setFrom(GeoPoint from) {
        this.from = from;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;
    }

    public void next() {
        pager.setCurrentItem(pager.getCurrentItem() + 1);
    }

    public void prev() {
        pager.setCurrentItem(pager.getCurrentItem() - 1);
    }

    public void attachToActivity() {
        act.hideActionBar();
        act.getPagerResultsFragment().clearMap();
        final MapzenProgressDialog progressDialog = new MapzenProgressDialog(act);
        progressDialog.show();

        String url = String.format(Locale.ENGLISH, urlTemplate, (int) Math.floor(app.getStoredZoomLevel()),
                from.getLatitude(), from.getLongitude(), destination.getLatitude(), destination.getLongitude());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setRouteFromResponse(response);
                if (route.foundRoute()) {
                    setInstructions(route.getRouteInstructions());
                    drawRoute();
                    progressDialog.dismiss();
                    displayRoute();
                } else {
                    Toast.makeText(act, act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    act.showActionBar();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        }
        );
        app.enqueueApiRequest(jsonObjectRequest);
    }

    private void drawRoute() {
        PathLayer layer = mapFragment.getPathLayer();
        layer.clearPath();
        for (double[] pair : route.getGeometry()) {
            layer.addPoint(new GeoPoint(pair[0], pair[1]));
        }
    }

    private void setRouteFromResponse(JSONObject response) {
        this.route = new Route(response);
    }

    private void displayRoute() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.bottom_container, this, "route")
                .commit();
    }

    private void clearRoute() {
        PathLayer layer = mapFragment.getPathLayer();
        layer.clearPath();
        mapFragment.updateMap();
    }
}

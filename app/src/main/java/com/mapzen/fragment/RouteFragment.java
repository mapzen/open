package com.mapzen.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.RoutesAdapter;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.util.RouteLayer;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.Locale;

public class RouteFragment extends Fragment {
    private ArrayList<Instruction> instructions;
    private MapFragment mapFragment;
    private TextView title, street;
    private int routeIndex;
    private Button nextBtn;
    private GeoPoint from, destination;
    private MapzenApplication app;
    private BaseActivity act;
    private String urlTemplate = "http://router.project-osrm.org/viaroute?z=%d"
            + "&output=json"
            + "&loc=%.6f,%.6f"
            + "&loc=%.6f,%.6f"
            + "&instructions=true";
    private float storedTilt = 0;
    private double storedBearing = 0;
    private int storedZoom = 0;
    private ViewPager pager;
    private RoutesAdapter adapter;

    public void setApp(MapzenApplication app) {
        this.app = app;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseActivity act = (BaseActivity) getActivity();
        act.showActionBar();
        clearRoute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        routeIndex = 0;
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        FrameLayout frame = (FrameLayout) container;
        frame.setVisibility(View.VISIBLE);

        pager = (ViewPager) rootView.findViewById(R.id.routes);
        adapter = new RoutesAdapter(getFragmentManager());
        adapter.setMap(mapFragment.getMap());
        adapter.setInstructions(instructions);
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Map map = mapFragment.getMap();
        MapPosition pos = map.getMapPostion();
        storedTilt = pos.tilt;
        storedBearing = pos.angle;
        storedZoom = pos.zoomLevel;
        return rootView;
    }

    public void setFrom(GeoPoint from) {
        this.from = from;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;
    }

    public void attachTo(BaseActivity act) {
        this.act = act;
        act.hideActionBar();
        act.getResultsFragment().hideResultsWrapper();
        final ProgressDialog progressDialog = new ProgressDialog(act);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Wait while loading...");
        progressDialog.show();

        String url = String.format(Locale.ENGLISH, urlTemplate, (int) Math.floor(app.getStoredZoomLevel()),
                from.getLatitude(), from.getLongitude(), destination.getLatitude(), destination.getLongitude());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Route route = new Route(response);
                setInstructions(route.getRouteInstructions());
                RouteLayer layer = mapFragment.getRouteLayer();
                layer.clear();
                for (double[] pair : route.getGeometry()) {
                    layer.addPoint(new GeoPoint(pair[0], pair[1]));
                }
                layer.updateMap();

                progressDialog.dismiss();
                displayRoute();
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

    private void displayRoute() {
        act.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(R.id.bottom_container, this, "route")
                .commit();
    }

    private void clearRoute() {
        RouteLayer layer = mapFragment.getRouteLayer();
        Map map = mapFragment.getMap();
        map.getViewport().setTilt(storedTilt);
        map.getViewport().setRotation(storedBearing);
        map.setMapPosition(destination.getLatitude(),
                destination.getLongitude(), Math.pow(2, storedZoom));
        layer.clear();
    }
}

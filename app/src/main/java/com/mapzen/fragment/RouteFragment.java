package com.mapzen.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import com.mapzen.util.ApiConstants;
import com.mapzen.util.Logger;
import com.mapzen.util.MapzenProgressDialog;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;

import java.util.ArrayList;

import static com.mapzen.activity.BaseActivity.ROUTE_STACK;
import static com.mapzen.activity.BaseActivity.SEARCH_RESULTS_STACK;
import static com.mapzen.util.ApiConstants.HTTP_SCHEMA;
import static com.mapzen.util.ApiConstants.ROUTE_CAR_PATH;
import static com.mapzen.util.ApiConstants.ROUTE_INSTRUCTIONS_KEY;
import static com.mapzen.util.ApiConstants.ROUTE_LOCATION_KEY;
import static com.mapzen.util.ApiConstants.ROUTE_OUTPUT_KEY;
import static com.mapzen.util.ApiConstants.ROUTE_URL;
import static com.mapzen.util.ApiConstants.ROUTE_ZOOMLEVEL;
import static com.mapzen.util.ApiConstants.TRUE;

public class RouteFragment extends BaseFragment {
    private ArrayList<Instruction> instructions;
    private GeoPoint from, destination;
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
        popSearchResultsStack();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(getRouteUrl(), null,
                new Response.Listener<JSONObject>() {
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

    private String getRouteUrl() {
        Uri.Builder url = new Uri.Builder();
        url.scheme(HTTP_SCHEMA).authority(ROUTE_URL).path(ROUTE_CAR_PATH);
        url.appendQueryParameter(ROUTE_ZOOMLEVEL, String.valueOf((int)
                Math.floor(app.getStoredZoomLevel())));
        url.appendQueryParameter(ROUTE_OUTPUT_KEY, ApiConstants.JSON);
        url.appendQueryParameter(ROUTE_LOCATION_KEY,
                String.valueOf(from.getLatitude()) + "," + String.valueOf(from.getLongitude()));
        url.appendQueryParameter(ROUTE_LOCATION_KEY, String.valueOf(
                destination.getLatitude()) + "," + String.valueOf(destination.getLongitude()));
        url.appendQueryParameter(ROUTE_INSTRUCTIONS_KEY, TRUE);
        return url.toString();
    }

    private void popSearchResultsStack() {
        act.getSupportFragmentManager()
                .popBackStack(SEARCH_RESULTS_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                .addToBackStack(ROUTE_STACK)
                .add(R.id.routes_container, this, "route")
                .commit();
    }

    private void clearRoute() {
        PathLayer layer = mapFragment.getPathLayer();
        layer.clearPath();
        mapFragment.updateMap();
    }
}

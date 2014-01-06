package com.mapzen.fragment;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.entity.RouteInstruction;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;

import java.util.ArrayList;

public class SearchResultItemFragment extends Fragment {
    private Feature feature;
    private MapFragment mapFragment;
    private MapzenApplication app;

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setApp(MapzenApplication app) {
        this.app = app;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_item, container, false);
        Feature.ViewHolder holder;
        if (view != null) {
            holder = new Feature.ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.place_title);
            holder.address = (TextView) view.findViewById(R.id.place_address);
            holder.button = (Button) view.findViewById(R.id.btn_route_go);
            view.setTag(holder);

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity act = (BaseActivity) getActivity();
                    ActionBar actionBar = act.getActionBar();
                    actionBar.hide();
                    act.getSearchResultsFragment().hideResultsWrapper();

                    ArrayList<GeoPoint> points = new ArrayList<GeoPoint>(2);
                    points.add(app.getLocationPoint());
                    points.add(feature.getGeoPoint());
                    final RouteInstruction routeInstruction = new RouteInstruction(
                            points, app.getStoredZoomLevel());

                    final ProgressDialog progressDialog = new ProgressDialog(act);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Wait while loading...");
                    progressDialog.show();
                    routeInstruction.fetchRoute(app, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    Route route = new Route(jsonObject);
                                    routeInstruction.draw(route);
                                    ArrayList<Instruction> instructions = route.getRouteInstructions();
                                    progressDialog.dismiss();
                                    RouteWidgetFragment routeWidgetFragment = new RouteWidgetFragment();
                                    routeWidgetFragment.setInstructions(instructions);
                                    routeWidgetFragment.setMapFragment(mapFragment);
                                    getFragmentManager().beginTransaction()
                                            .add(R.id.container, routeWidgetFragment)
                                            .commit();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    progressDialog.dismiss();
                                }
                            }
                    );

                    routeInstruction.setLayer(mapFragment.getRouteLayer());
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((BaseActivity) getActivity()).showPlace(feature, true);
                }
            });
        } else {
            holder = (Feature.ViewHolder) view.getTag();
        }

        holder.setFromFeature(feature);
        return view;
    }

    public Feature getFeature() {
        return feature;
    }
}

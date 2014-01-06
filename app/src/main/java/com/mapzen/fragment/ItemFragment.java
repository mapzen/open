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

import org.json.JSONObject;
import org.oscim.core.GeoPoint;

import java.util.ArrayList;

public class ItemFragment extends Fragment {
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
            holder.setTitle((TextView) view.findViewById(R.id.place_title));
            holder.setAddress((TextView) view.findViewById(R.id.place_address));
            Button button = (Button) view.findViewById(R.id.btn_route_go);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity act = (BaseActivity) getActivity();
                    ActionBar actionBar = act.getActionBar();
                    actionBar.hide();
                    act.getResultsFragment().hideResultsWrapper();

                    ArrayList<GeoPoint> points = new ArrayList<GeoPoint>(2);
                    points.add(app.getLocationPoint());
                    points.add(feature.getGeoPoint());
                    final RouteInstruction routeInstructionInstruction = new RouteInstruction(
                            points, app.getStoredZoomLevel());

                    final ProgressDialog progressDialog = new ProgressDialog(act);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Wait while loading...");
                    progressDialog.show();
                    routeInstructionInstruction.fetchRoute(app, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    com.mapzen.osrm.Route route = new com.mapzen.osrm.Route(jsonObject);
                                    routeInstructionInstruction.draw(route);
                                    ArrayList<Instruction> instructions = route.getRouteInstructions();
                                    progressDialog.dismiss();
                                    RouteFragment routeFragment = new RouteFragment();
                                    routeFragment.setInstructions(instructions);
                                    routeFragment.setMapFragment(mapFragment);
                                    getFragmentManager().beginTransaction()
                                            .add(R.id.container, routeFragment)
                                            .commit();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    progressDialog.dismiss();
                                }
                            }
                    );

                    routeInstructionInstruction.setLayer(mapFragment.getRouteLayer());
                }
            });
            holder.setButton(button);
            view.setTag(holder);


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

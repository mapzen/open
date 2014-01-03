package com.mapzen.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.mapzen.util.RouteLayer;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

import java.util.ArrayList;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class SearchResultItemFragment extends Fragment {
    private Feature feature;
    private MapFragment mapFragment;
    private MapzenApplication app;

    public SearchResultItemFragment(Feature feature) {
        this.feature = feature;
    }

    public void setApp(MapzenApplication app) {
        this.app = app;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    private Response.Listener<JSONObject> getSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                RouteLayer routeLayer = mapFragment.getRouteLayer();
                Route route = new Route(jsonObject);
                for(double[] pair : route.getGeometry()) {
                    Log.v(LOG_TAG, String.format("point %.6f : %.6f", pair[0], pair[1]));
                    routeLayer.addPoint(new GeoPoint(pair[0], pair[1]));
                }
                routeLayer.updateMap();
            }
        };
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
                    RouteInstruction routeInstruction = new RouteInstruction(
                            points, app.getStoredZoomLevel());

                    routeInstruction.fetchRoute(app, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Route route = new Route(jsonObject);
                            ArrayList<Instruction> instructions = route.getRouteInstructions();
                            Instruction instruction = instructions.get(0);

                            getFragmentManager().beginTransaction()
                                    .add(R.id.container, new PlaceholderFragment(instructions, mapFragment))
                                    .commit();

                        }
                    },new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                        }
                    });

                    routeInstruction.setLayer(mapFragment.getRouteLayer());
                    routeInstruction.draw(app);
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

    public static class PlaceholderFragment extends Fragment {
        private ArrayList<Instruction> instructions;
        private MapFragment mapFragment;
        private TextView title, street;
        private int routeIndex;
        private Button nextBtn;

        public PlaceholderFragment(ArrayList<Instruction> instructions, MapFragment mapFragment) {
            this.instructions = instructions;
            this.mapFragment = mapFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            routeIndex = 0;
            View rootView = inflater.inflate(R.layout.route_widget, container, false);
            FrameLayout frame = (FrameLayout)container;
            frame.setVisibility(View.VISIBLE);
            title = (TextView) rootView.findViewById(R.id.instruction_title);
            street = (TextView) rootView.findViewById(R.id.instruction_street);
            nextBtn = (Button) rootView.findViewById(R.id.next_btn);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    routeIndex++;
                    setRoute(routeIndex);
                }
            });
            setRoute(routeIndex);
            return rootView;
        }

        private void setRoute(int index) {
            title.setText(instructions.get(index).getHumanTurnInstruction());
            street.setText(instructions.get(index).getName());

            double[] firstPoint = instructions.get(index).getPoint();
            Map map = mapFragment.getMap();
            map.setMapPosition(firstPoint[0], firstPoint[1], Math.pow(2, 19));
            map.getViewport().setTilt(150.0f);
            map.getViewport().setRotation(instructions.get(index).getBearing());
        }
    }

    public Feature getFeature() {
        return feature;
    }
}

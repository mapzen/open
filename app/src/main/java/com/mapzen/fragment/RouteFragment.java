package com.mapzen.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapzen.R;
import com.mapzen.entity.Feature;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.util.DisplayHelper;
import com.mapzen.util.Logger;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.List;

import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.activity.BaseActivity.ROUTE_STACK;
import static com.mapzen.entity.Feature.NAME;

public class RouteFragment extends BaseFragment implements DirectionListFragment.DirectionListener,
        ViewPager.OnPageChangeListener {
    public static final int WALKING_THRESH_HOLD = 10;
    private ArrayList<Instruction> instructions;
    private ViewPager pager;
    private Button button;
    private RoutesAdapter adapter;
    private Route route;
    private LocationReceiver locationReceiver;
    private Feature feature;
    private TextView distanceLeftView;
    public static final int ROUTE_ZOOM_LEVEL = 17;

    public void setInstructions(ArrayList<Instruction> instructions) {
        Logger.d("instructions: " + instructions.toString());
        this.instructions = instructions;
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationReceiver();
        act.hideActionBar();
        drawRoute();
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public GeoPoint getDestinationPoint() {
        return feature.getGeoPoint();
    }

    private Location getNextTurnTo(Instruction nextInstruction) {
        Location nextTurn = new Location(getResources().getString(R.string.application_name));
        nextTurn.setLatitude(nextInstruction.getPoint()[0]);
        nextTurn.setLongitude(nextInstruction.getPoint()[1]);
        return nextTurn;
    }

    public void onLocationChanged(Location location) {
        Logger.d("RouteFragment::onLocationChangeLocation" + instructions.toString());
        for (Instruction instruction : instructions) {
            Location nextTurn = getNextTurnTo(instruction);
            if (location != null && nextTurn != null) {
                int distanceToNextTurn = (int) Math.floor(location.distanceTo(nextTurn));
                if (distanceToNextTurn > WALKING_THRESH_HOLD) {
                    Logger.d("RouteFragment::onLocationChangeLocation: " +
                            "outside defined radius");
                } else {
                    Logger.d("RouteFragment::onLocationChangeLocation: " +
                            "inside defined radius advancing");
                    advanceTo(instructions.indexOf(instruction));
                }
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "new current location: " + location.toString());
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "next turn: " + nextTurn.toString());
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "distance to next turn: " + String.valueOf(distanceToNextTurn));
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "threshold: " + String.valueOf(WALKING_THRESH_HOLD));
            } else {
                if (location == null) {
                    Logger.d("RouteFragment::onLocationChangeLocation: **next turn** is null screw it");
                }
                if (nextTurn == null) {
                    Logger.d("RouteFragment::onLocationChangeLocation: **location** is null screw it");
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        act.unregisterReceiver(locationReceiver);
        clearRoute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        button = (Button) rootView.findViewById(R.id.view_steps);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDirectionListFragment();
            }
        });
        adapter = new RoutesAdapter(act, this, instructions);
        pager = (ViewPager) rootView.findViewById(R.id.routes);
        TextView destinationName = (TextView) rootView.findViewById(R.id.destination_name);
        destinationName.setText(feature.getProperty(NAME));
        distanceLeftView = (TextView) rootView.findViewById(R.id.destination_distance);
        if (route != null) {
            distanceLeftView.setText(String.valueOf(route.getTotalDistance()));
        }
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        adapter.notifyDataSetChanged();

        return rootView;
    }

    private void showDirectionListFragment() {
        final Fragment fragment = DirectionListFragment.newInstance(instructions, this);
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.full_list, fragment, DirectionListFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.showActionBar();
        clearRoute();
    }

    public void next() {
        changeDistance(-instructions.get(pager.getCurrentItem()).getDistance());
        pager.setCurrentItem(pager.getCurrentItem() + 1);
    }

    public void advanceTo(int i) {
        pager.setCurrentItem(i);
    }

    private void changeDistance(int difference) {
        int newDistance = Integer.parseInt(distanceLeftView.getText().toString()) + difference;
        distanceLeftView.setText(String.valueOf(newDistance));
    }

    public void prev() {
        int nextItemIndex = pager.getCurrentItem() - 1;
        pager.setCurrentItem(nextItemIndex);
        changeDistance(instructions.get(nextItemIndex).getDistance());
    }

    public int getCurrentItem() {
        return pager.getCurrentItem();
    }

    public void onRouteSuccess(JSONObject rawRoute) {
        this.route = new Route(rawRoute);
        if (route.foundRoute()) {
            setInstructions(route.getRouteInstructions());
            drawRoute();
            displayRoute();
        } else {
            Toast.makeText(act, act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
            act.dismissProgressDialog();
            act.showActionBar();
        }
    }

    public void attachToActivity() {
    }

    private void drawRoute() {
        PathLayer layer = mapFragment.getPathLayer();
        layer.clearPath();
        if (route != null) {
            for (double[] pair : route.getGeometry()) {
                layer.addPoint(new GeoPoint(pair[0], pair[1]));
            }
        }
    }

    public Route getRoute() {
        return route;
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

    @Override
    public void onInstructionSelected(int index) {
        pager.setCurrentItem(index, true);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        double[] point = instructions.get(i).getPoint();
        Map map = act.getMap();
        map.setMapPosition(point[0], point[1], Math.pow(2, ROUTE_ZOOM_LEVEL));
        map.getViewport().setRotation(instructions.get(i).getBearing());
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private static class RoutesAdapter extends PagerAdapter {
        private List<Instruction> instructions = new ArrayList<Instruction>();
        private RouteFragment parent;
        private Context context;

        public RoutesAdapter(Context context, RouteFragment parent, List<Instruction> instructions) {
            this.context = context;
            this.instructions = instructions;
            this.parent = parent;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Instruction instruction = instructions.get(position);
            View view = View.inflate(context, R.layout.fragment_instruction, null);

            TextView fullInstruction = (TextView) view.findViewById(R.id.full_instruction);
            fullInstruction.setText(instruction.getFullInstruction());

            ImageView turnIcon = (ImageView) view.findViewById(R.id.turn_icon);
            turnIcon.setImageResource(DisplayHelper.getRouteDrawable(context,
                    instruction.getTurnInstruction(), DisplayHelper.IconStyle.WHITE));

            if (instructions.size() != position + 1) {
                ImageButton next = (ImageButton) view.findViewById(R.id.route_next);
                next.setVisibility(View.VISIBLE);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parent.next();
                    }
                });
            }

            if (position > 0) {
                ImageButton prev = (ImageButton) view.findViewById(R.id.route_previous);
                prev.setVisibility(View.VISIBLE);
                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parent.prev();
                    }
                });
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return instructions.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void initLocationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_MAPZEN_UPDATES_LOCATION);
        locationReceiver = new LocationReceiver();
        act.registerReceiver(locationReceiver, filter);
    }

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Location location = bundle.getParcelable("location");
            onLocationChanged(location);
        }
    }
}

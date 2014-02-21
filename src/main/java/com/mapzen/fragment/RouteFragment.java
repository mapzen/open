package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.util.DisplayHelper;
import com.mapzen.util.LocationDatabaseHelper;
import com.mapzen.util.Logger;
import com.mapzen.widget.DistanceView;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.Feature.NAME;

public class RouteFragment extends BaseFragment implements DirectionListFragment.DirectionListener,
        ViewPager.OnPageChangeListener {
    public static final String TAG = RouteFragment.class.getSimpleName();
    public static final int WALKING_ADVANCE_DEFAULT_RADIUS = 15;
    public static final int WALKING_LOST_THRESHOLD = 70;
    public static final int ROUTE_ZOOM_LEVEL = 17;

    @InjectView(R.id.overflow_menu) ImageButton overflowMenu;
    @InjectView(R.id.routes) ViewPager pager;

    private ArrayList<Instruction> instructions;
    private RoutesAdapter adapter;
    private Route route;
    private LocationReceiver locationReceiver;
    private Feature feature;
    private DistanceView distanceLeftView;
    private int previousPosition;
    private boolean locationPassThrough = false;
    private boolean hasFoundPath = false;

    public static RouteFragment newInstance(BaseActivity act, Feature feature) {
        final RouteFragment fragment = new RouteFragment();
        fragment.setAct(act);
        fragment.setMapFragment(act.getMapFragment());
        fragment.setFeature(feature);
        return fragment;
    }

    @OnClick(R.id.overflow_menu)
    @SuppressWarnings("unused")
    public void onClickOverFlowMenu() {
        PopupMenu popup = new PopupMenu(getActivity(), overflowMenu);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.route_options_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.route_menu_steps) {
                    showDirectionListFragment();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationReceiver();
        act.hideActionBar();
        act.deactivateMapLocationUpdates();
        drawRoute();
    }

    @Override
    public void onPause() {
        super.onPause();
        act.unregisterReceiver(locationReceiver);
        act.activateMapLocationUpdates();
        clearRoute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.showActionBar();
        clearRoute();
    }

    public int getWalkingAdvanceRadius() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        final String walkingAdvanceString =
                prefs.getString(act.getString(R.string.settings_key_walking_advance_radius),
                        Integer.toString(WALKING_ADVANCE_DEFAULT_RADIUS));
        return Integer.valueOf(walkingAdvanceString);
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        Logger.d("instructions: " + instructions.toString());
        this.instructions = instructions;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
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

    public void setLocationPassThrough(boolean locationPassThrough) {
        this.locationPassThrough = locationPassThrough;
    }

    private Location snapTo(Location location) {
        if (!locationPassThrough) {
            Instruction instruction = instructions.get(getCurrentItem());
            double[] locationPoint = {location.getLatitude(), location.getLongitude()};
            Logger.d("RouteFragment::onLocationChange: current location: "
                    + String.valueOf(location.getLatitude()) + " ,"
                    + String.valueOf(location.getLongitude()));
            Logger.d("RouteFragment::onLocationChange: reference location: "
                    + instruction.toString());
            double[] onRoadPoint;
            onRoadPoint = instruction.snapTo(locationPoint, -90);
            if (onRoadPoint == null) {
                onRoadPoint = instruction.snapTo(locationPoint, 90);
            }
            Location correctedLocation = new Location("Corrected");
            correctedLocation.setLatitude(onRoadPoint[0]);
            correctedLocation.setLongitude(onRoadPoint[1]);
            return correctedLocation;
        }
        return location;
    }

    private Location getStartLocation() {
        Location beginning = new Location("begin point");
        beginning.setLatitude(route.getStartCoordinates()[0]);
        beginning.setLongitude(route.getStartCoordinates()[1]);
        return beginning;
    }

    public void onLocationChanged(Location location) {
        Location correctedLocation = snapTo(location);
        if (act.isInDebugMode()) {
            storeLocationInfo(location, correctedLocation);
        }
        if (correctedLocation != null) {
            getMapController().setLocation(correctedLocation);
            mapFragment.findMe();
            hasFoundPath = true;
            Logger.d("RouteFragment::onLocationChange: Corrected: " + correctedLocation.toString());
        } else {
            Logger.d("RouteFragment::onLocationChange: ambigous location");
        }

        if (WALKING_LOST_THRESHOLD < location.distanceTo(correctedLocation) &&
                location.getAccuracy() < getWalkingAdvanceRadius()) {
            // execute reroute query and reset the path
            Logger.d("RouteFragment::onLocationChange: probably off course");
        }

        if (!hasFoundPath && getStartLocation().distanceTo(location) > getWalkingAdvanceRadius()) {
            Logger.d("RouteFragment::onLocationChange: hasn't hit first location and is"
                    + "probably off course");
        }

        Location nextTurn = getNextTurnTo(instructions.get(pager.getCurrentItem() + 1));
        if (correctedLocation != null && nextTurn != null) {
            int distanceToNextTurn = (int) Math.floor(correctedLocation.distanceTo(nextTurn));
            if (distanceToNextTurn > getWalkingAdvanceRadius()) {
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "outside defined radius");
            } else {
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "inside defined radius advancing");
                goToNextInstruction();
            }
            Logger.d("RouteFragment::onLocationChangeLocation: " +
                    "new current location: " + location.toString());
            Logger.d("RouteFragment::onLocationChangeLocation: " +
                    "next turn: " + nextTurn.toString());
            Logger.d("RouteFragment::onLocationChangeLocation: " +
                    "distance to next turn: " + String.valueOf(distanceToNextTurn));
            Logger.d("RouteFragment::onLocationChangeLocation: " +
                    "threshold: " + String.valueOf(getWalkingAdvanceRadius()));
        } else {
            if (correctedLocation == null) {
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "**next turn** is null screw it");
            }
            if (nextTurn == null) {
                Logger.d("RouteFragment::onLocationChangeLocation: " +
                        "**location** is null screw it");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        ButterKnife.inject(this, rootView);
        adapter = new RoutesAdapter(act, this, instructions);
        TextView destinationName = (TextView) rootView.findViewById(R.id.destination_name);
        destinationName.setText(feature.getProperty(NAME));
        distanceLeftView = (DistanceView) rootView.findViewById(R.id.destination_distance);
        distanceLeftView.setRealTime(true);
        if (route != null) {
            distanceLeftView.setDistance(route.getTotalDistance());
        }
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        adapter.notifyDataSetChanged();
        previousPosition = pager.getCurrentItem();
        return rootView;
    }

    private void showDirectionListFragment() {
        final Fragment fragment = DirectionListFragment.newInstance(instructions, this);
        act.getSupportFragmentManager().beginTransaction()
                .add(R.id.full_list, fragment, DirectionListFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    public void goToNextInstruction() {
        pager.setCurrentItem(pager.getCurrentItem() + 1);
    }

    private void changeDistance(int difference) {
        if (!distanceLeftView.getText().toString().isEmpty()) {
            int newDistance = distanceLeftView.getDistance() + difference;
            distanceLeftView.setDistance(newDistance);
        }
    }

    public void goToPrevInstruction() {
        int nextItemIndex = pager.getCurrentItem() - 1;
        pager.setCurrentItem(nextItemIndex);
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
            setMapPerspectiveForInstruction(instructions.get(0));
        } else {
            Toast.makeText(act, act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
            act.dismissProgressDialog();
            act.showActionBar();
        }
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
                .addToBackStack(null)
                .add(R.id.routes_container, this, RouteFragment.TAG)
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
        if (previousPosition > i) {
            changeDistance(instructions.get(i + 1).getDistance());
        } else if (previousPosition < i) {
            changeDistance(-instructions.get(previousPosition).getDistance());
        }
        previousPosition = i;
        setMapPerspectiveForInstruction(instructions.get(i));
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    public void setMapPerspectiveForInstruction(Instruction instruction) {
        Map map = act.getMap();
        double[] point = instruction.getPoint();
        map.setMapPosition(point[0], point[1], Math.pow(2, ROUTE_ZOOM_LEVEL));
        map.viewport().setRotation(instruction.getRotationBearing());
        map.updateMap(true);
    }

    private void initLocationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_MAPZEN_UPDATES_LOCATION);
        locationReceiver = new LocationReceiver();
        act.registerReceiver(locationReceiver, filter);
    }

    private void storeLocationInfo(Location location, Location correctedLocation) {
        SQLiteDatabase db = act.getDb();
        db.execSQL(LocationDatabaseHelper.insertSQLForLocationCorrection(location,
                correctedLocation, instructions.get(pager.getCurrentItem())));
    }

    private static class RoutesAdapter extends PagerAdapter {
        private List<Instruction> instructions = new ArrayList<Instruction>();
        private RouteFragment parent;
        private Context context;

        public RoutesAdapter(Context context, RouteFragment parent,
                List<Instruction> instructions) {
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
            View view = View.inflate(context, R.layout.instruction, null);

            if (position == instructions.size() - 1) {
                view.setBackgroundColor(context.getResources().getColor(R.color.destination_green));
            } else {
                view.setBackgroundColor(context.getResources().getColor(R.color.dark_gray));
            }

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
                        parent.goToNextInstruction();
                    }
                });
            }

            if (position > 0) {
                ImageButton prev = (ImageButton) view.findViewById(R.id.route_previous);
                prev.setVisibility(View.VISIBLE);
                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parent.goToPrevInstruction();
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

    private class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Location location = bundle.getParcelable("location");
            onLocationChanged(location);
        }
    }
}

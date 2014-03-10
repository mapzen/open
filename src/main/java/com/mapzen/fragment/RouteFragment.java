package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.osrm.Instruction;
import com.mapzen.osrm.Route;
import com.mapzen.speakerbox.Speakerbox;
import com.mapzen.util.DisplayHelper;
import com.mapzen.util.GearAgentService;
import com.mapzen.util.GearServiceSocket;
import com.mapzen.util.Logger;
import com.mapzen.widget.DistanceView;

import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.Feature.NAME;
import static com.mapzen.util.DatabaseHelper.COLUMN_LAT;
import static com.mapzen.util.DatabaseHelper.COLUMN_LNG;
import static com.mapzen.util.DatabaseHelper.COLUMN_POSITION;
import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.util.DatabaseHelper.TABLE_LOCATIONS;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTE_GEOMETRY;
import static com.mapzen.util.DatabaseHelper.valuesForLocationCorrection;

public class RouteFragment extends BaseFragment implements DirectionListFragment.DirectionListener,
        ViewPager.OnPageChangeListener {
    public static final String TAG = RouteFragment.class.getSimpleName();
    public static final int WALKING_ADVANCE_DEFAULT_RADIUS = 15;
    public static final int WALKING_LOST_THRESHOLD = 70;
    public static final int ROUTE_ZOOM_LEVEL = 17;
    public static final String ROUTE_TAG = "route";

    @InjectView(R.id.overflow_menu) ImageButton overflowMenu;
    @InjectView(R.id.routes) ViewPager pager;

    private ArrayList<Instruction> instructions;
    private RoutesAdapter adapter;
    private Route route;
    private LocationReceiver locationReceiver;
    private Feature feature;
    private DistanceView distanceLeftView;
    private int previousPosition;
    private long routeId;
    private Set<Instruction> proximityAlerts = new HashSet<Instruction>();
    private Set<Instruction> seenInstructions = new HashSet<Instruction>();

    Speakerbox speakerbox;

    // FOR testing
    private int itemIndex = 0;
    private Set<Instruction> flippedInstructions = new HashSet<Instruction>();

    public static RouteFragment newInstance(BaseActivity act, Feature feature) {
        final RouteFragment fragment = new RouteFragment();
        fragment.setAct(act);
        fragment.setMapFragment(act.getMapFragment());
        fragment.setFeature(feature);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        ButterKnife.inject(this, rootView);
        adapter = new RoutesAdapter(act, instructions);
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
        initSpeakerbox();
        return rootView;
    }

    private void initSpeakerbox() {
        speakerbox = new Speakerbox(getActivity());
        addRemixPatterns();
        checkIfVoiceNavigationIsEnabled();
        playFirstInstruction();
    }

    private void addRemixPatterns() {
        speakerbox.remix(" mi", " miles");
        speakerbox.remix(" 1 miles", " 1 mile");
        speakerbox.remix(" ft", " feet");
    }

    private void checkIfVoiceNavigationIsEnabled() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        boolean voiceNavigationEnabled =
                prefs.getBoolean(getString(R.string.settings_voice_navigation_key), false);

        if (voiceNavigationEnabled) {
            speakerbox.unmute();
        } else {
            speakerbox.mute();
        }
    }

    private void playFirstInstruction() {
        if (instructions != null && instructions.size() > 0) {
            speakerbox.play(instructions.get(0).getFullInstruction());
        }
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
        act.getDb().beginTransaction();
        drawRoute();
        initProximityAlerts();
    }

    @Override
    public void onPause() {
        super.onPause();
        act.unregisterReceiver(locationReceiver);
        act.activateMapLocationUpdates();
        act.getDb().setTransactionSuccessful();
        act.getDb().endTransaction();
        clearRoute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.showActionBar();
        clearRoute();
    }

    public long getRouteId() {
        return routeId;
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

    private void initProximityAlerts() {
        for (Instruction instruction : instructions) {
            proximityAlerts.add(instruction);
        }
    }

    private Location snapTo(Location location) {
        double[] onRoadPoint;
        onRoadPoint = route.snapToRoute(
            new double[] {location.getLatitude(), location.getLongitude()}
        );

        if (onRoadPoint != null) {
            Location correctedLocation = new Location("Corrected");
            correctedLocation.setLatitude(onRoadPoint[0]);
            correctedLocation.setLongitude(onRoadPoint[1]);
            return correctedLocation;
        }
        return location;
    }

    private void manageMap(Location location, Location originalLocation) {
        if (location != null) {
            getMapController().setLocation(location);
            mapFragment.findMe();
            Logger.logToDatabase(act, ROUTE_TAG, "RouteFragment::onLocationChange: Corrected: "
                    + location.toString());
        } else {
            Logger.logToDatabase(act, ROUTE_TAG,
                    "RouteFragment::onLocationChange: Unable to Correct: location: "
                    + originalLocation.toString());
        }
    }

    public Set<Instruction> getProximityAlerts() {
        return proximityAlerts;
    }

    public void onLocationChanged(Location location) {
        Location correctedLocation = snapTo(location);
        storeLocationInfo(location, correctedLocation);
        manageMap(correctedLocation, location);
        StringBuilder debugStringBuilder = new StringBuilder();

        // No corrected location
        if (correctedLocation == null) {
            Logger.logToDatabase(act, ROUTE_TAG, "RouteFragment::onLocationChangeLocation: " +
                    "**correctedLocation** is null");
            return;
        }

        Instruction closestInstruction = null;
        int closestDistance = (int) 1e8;
        for (Instruction instruction : proximityAlerts) {
            Location temporaryLocationObj = new Location("tmp");
            temporaryLocationObj.setLatitude(instruction.getPoint()[0]);
            temporaryLocationObj.setLongitude(instruction.getPoint()[1]);
            final int distanceToTurn =
                    (int) Math.floor(correctedLocation.distanceTo(temporaryLocationObj));
            Logger.logToDatabase(act, ROUTE_TAG, String.valueOf(distanceToTurn)
                    + " distance to instruction: " + instruction.toString()
                    + " threshold is: " + String.valueOf(getWalkingAdvanceRadius()));
            Logger.logToDatabase(act, ROUTE_TAG, "current closest distance: "
                    + String.valueOf(closestDistance));
            if (closestInstruction != null) {
                Logger.logToDatabase(act, ROUTE_TAG, "current closest instruction: "
                        + closestInstruction.toString());
            } else {
                Logger.logToDatabase(act, ROUTE_TAG, "current closest instruction: null");
            }
            if (distanceToTurn < closestDistance) {
                closestDistance = distanceToTurn;
                closestInstruction = instruction;
            }
        }

        if (closestInstruction != null) {
            debugStringBuilder.append("Closest instruction is: " + closestInstruction.getName());
            debugStringBuilder.append(", distance: " + String.valueOf(closestDistance));
        }

        if (closestDistance < getWalkingAdvanceRadius()) {
            Logger.logToDatabase(act, ROUTE_TAG, "paging to instruction: "
                    + closestInstruction.toString());
            final int instructionIndex = instructions.indexOf(closestInstruction);
            pager.setCurrentItem(instructionIndex);
            itemIndex = instructionIndex;
            if (!seenInstructions.contains(closestInstruction)) {
                seenInstructions.add(closestInstruction);
            }
        }

        final Iterator it = seenInstructions.iterator();
        while (it.hasNext()) {
            Instruction instruction = (Instruction) it.next();
            final Location l = new Location("temp");
            l.setLatitude(instruction.getPoint()[0]);
            l.setLongitude(instruction.getPoint()[1]);
            final int distance = (int) Math.floor(l.distanceTo(correctedLocation));
            debugStringBuilder.append("\n");
            debugStringBuilder.append("seen instruction: " + instruction.getName());
            debugStringBuilder.append(" distance: " + String.valueOf(distance));
            if (distance > getWalkingAdvanceRadius()) {
                Logger.logToDatabase(act, ROUTE_TAG, "post language: " +
                        instruction.toString());
                act.appendToDebugView("post language for: " + instruction.toString());
                flipInstructionToAfter(instruction);
            }
        }

        act.writeToDebugView(debugStringBuilder.toString());
        logForDebugging(location, correctedLocation);
    }

    private void flipInstructionToAfter(Instruction instruction) {
        final int index = instructions.indexOf(instruction);
        if (flippedInstructions.contains(instruction)) {
            return;
        }
        flippedInstructions.add(instruction);
        if (pager.getCurrentItem() == index) {
            speakerbox.play(instruction.getFullInstructionAfterAction());
        }

        View view = pager.findViewWithTag(
                "Instruction_" + String.valueOf(index));

        if (view != null) {
            TextView fullBefore = (TextView) view.findViewById(R.id.full_instruction);
            TextView fullAfter = (TextView) view.findViewById(R.id.full_instruction_after_action);
            fullBefore.setVisibility(View.GONE);
            fullAfter.setVisibility(View.VISIBLE);
            ImageView turnIconBefore = (ImageView) view.findViewById(R.id.turn_icon);
            turnIconBefore.setVisibility(View.GONE);
            ImageView turnIconAfter = (ImageView) view.findViewById(R.id.turn_icon_after_action);
            turnIconAfter.setVisibility(View.VISIBLE);
        }
    }

    private void logForDebugging(Location location, Location correctedLocation) {
        Logger.logToDatabase(act, ROUTE_TAG, "RouteFragment::onLocationChangeLocation: "
                + "new corrected location: " + correctedLocation.toString()
                + " from original: " + location.toString());
        Logger.logToDatabase(act, ROUTE_TAG, "RouteFragment::onLocationChangeLocation: " +
                "threshold: " + String.valueOf(getWalkingAdvanceRadius()));
        for (Instruction instruction : instructions) {
            Logger.logToDatabase(act, ROUTE_TAG, "RouteFragment::onLocationChangeLocation: " +
                    "turnPoint: " + instruction.toString());
        }
    }

    private void showDirectionListFragment() {
        final Fragment fragment = DirectionListFragment.newInstance(instructions, this);
        act.getSupportFragmentManager().beginTransaction()
                .add(R.id.full_list, fragment, DirectionListFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void changeDistance(int difference) {
        if (!distanceLeftView.getText().toString().isEmpty()) {
            int newDistance = distanceLeftView.getDistance() + difference;
            distanceLeftView.setDistance(newDistance);
        }
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void onRouteSuccess(JSONObject rawRoute) {
        if (act.isInDebugMode()) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(COLUMN_RAW, rawRoute.toString());
            routeId = act.getDb().insert(TABLE_ROUTES, null, insertValues);
        }
        setRoute(new Route(rawRoute));
        if (route.foundRoute()) {
            setInstructions(route.getRouteInstructions());
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
            ArrayList<double[]> geometry = route.getGeometry();
            for (int index = 0; index < geometry.size(); index++) {
                double[] pair = geometry.get(index);
                addCoordinateToDatabase(pair, index);
                layer.addPoint(new GeoPoint(pair[0], pair[1]));
            }
        }
    }

    private void addCoordinateToDatabase(double[] pair, int pos) {
        if (act.isInDebugMode()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROUTE_ID, routeId);
            values.put(COLUMN_POSITION, pos);
            values.put(COLUMN_LAT, pair[0]);
            values.put(COLUMN_LNG, pair[1]);
            act.getDb().insert(TABLE_ROUTE_GEOMETRY, null, values);
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
        speakerbox.play(instructions.get(i).getFullInstruction());
        try {
            ServiceConnection conn = ServiceImpl.mConnection;
            if (conn != null) {
                int channelId = ServiceImpl.CHANNEL_ID;
                conn.send(channelId, instructions.get(i).getGearJson().toString().getBytes());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

    public int getItemIndex() {
        return itemIndex;
    }

    public Set<Instruction> getFlippedInstructions() {
        return flippedInstructions;
    }

    private void initLocationReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_MAPZEN_UPDATES_LOCATION);
        locationReceiver = new LocationReceiver();
        act.registerReceiver(locationReceiver, filter);
    }

    private void storeLocationInfo(Location location, Location correctedLocation) {
        if (act.isInDebugMode()) {
            act.getDb().insert(TABLE_LOCATIONS, null,
                    valuesForLocationCorrection(location,
                            correctedLocation, instructions.get(pager.getCurrentItem()), routeId));
        }
    }

    private static class RoutesAdapter extends PagerAdapter {
        private List<Instruction> instructions = new ArrayList<Instruction>();
        private Context context;
        private Instruction currentInstruction;

        public RoutesAdapter(Context context, List<Instruction> instructions) {
            this.context = context;
            this.instructions = instructions;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            currentInstruction = instructions.get(position);
            View view = View.inflate(context, R.layout.instruction, null);

            if (position == instructions.size() - 1) {
                view.setBackgroundColor(context.getResources().getColor(R.color.destination_green));
            } else {
                view.setBackgroundColor(context.getResources().getColor(R.color.dark_gray));
            }

            TextView fullInstruction = (TextView) view.findViewById(R.id.full_instruction);
            fullInstruction.setText(
                    getFullInstructionWithBoldName(currentInstruction.getFullInstruction()));

            TextView fullInstructionAfterAction =
                    (TextView) view.findViewById(R.id.full_instruction_after_action);
            fullInstructionAfterAction.setText(
                    getFullInstructionWithBoldName(
                            currentInstruction.getFullInstructionAfterAction()));

            ImageView turnIcon = (ImageView) view.findViewById(R.id.turn_icon);
            turnIcon.setImageResource(DisplayHelper.getRouteDrawable(context,
                    currentInstruction.getTurnInstruction(), DisplayHelper.IconStyle.WHITE));

            ImageView turnIconAfterAction =
                    (ImageView) view.findViewById(R.id.turn_icon_after_action);
            turnIconAfterAction.setImageResource(DisplayHelper.getRouteDrawable(context,
                    10, DisplayHelper.IconStyle.WHITE));

            view.setTag("Instruction_" + String.valueOf(position));
            container.addView(view);
            return view;
        }

        private SpannableStringBuilder getFullInstructionWithBoldName(String fullInstruction) {
            final String name = currentInstruction.getName();
            final int startOfName = fullInstruction.indexOf(name);
            final int endOfName = startOfName + name.length();
            final StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);

            final SpannableStringBuilder ssb = new SpannableStringBuilder(fullInstruction);
            ssb.setSpan(boldStyleSpan, startOfName, endOfName, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            return ssb;
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

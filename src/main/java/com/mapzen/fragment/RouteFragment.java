package com.mapzen.fragment;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.osrm.Callback;
import com.mapzen.osrm.Direction;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATES_LOCATION;
import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.util.DatabaseHelper.COLUMN_LAT;
import static com.mapzen.util.DatabaseHelper.COLUMN_LNG;
import static com.mapzen.util.DatabaseHelper.COLUMN_POSITION;
import static com.mapzen.util.DatabaseHelper.COLUMN_RAW;
import static com.mapzen.util.DatabaseHelper.COLUMN_ROUTE_ID;
import static com.mapzen.util.DatabaseHelper.COLUMN_TABLE_ID;
import static com.mapzen.util.DatabaseHelper.TABLE_LOCATIONS;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTES;
import static com.mapzen.util.DatabaseHelper.TABLE_ROUTE_GEOMETRY;
import static com.mapzen.util.DatabaseHelper.valuesForLocationCorrection;

public class RouteFragment extends BaseFragment implements DirectionListFragment.DirectionListener,
        ViewPager.OnPageChangeListener, Callback {
    public static final String TAG = RouteFragment.class.getSimpleName();
    public static final int ROUTE_ZOOM_LEVEL = 17;
    public static final String ROUTE_TAG = "route";

    @InjectView(R.id.overflow_menu) ImageButton overflowMenu;
    @InjectView(R.id.routes) ViewPager pager;
    @InjectView(R.id.resume_button) Button resume;

    private ArrayList<Instruction> instructions;
    private RoutesAdapter adapter;
    private Route route;
    private LocationReceiver locationReceiver;
    private SimpleFeature simpleFeature;
    private DistanceView distanceLeftView;
    private int previousPosition;
    private String routeId;
    private Set<Instruction> proximityAlerts = new HashSet<Instruction>();
    private Set<Instruction> seenInstructions = new HashSet<Instruction>();
    private int pagerPositionWhenPaused = 0;

    Speakerbox speakerbox;

    private Set<Instruction> flippedInstructions = new HashSet<Instruction>();
    private boolean isRouting = false;
    private boolean autoPaging = true;

    public static Direction.Router router = Direction.getRouter();

    public static RouteFragment newInstance(BaseActivity act, SimpleFeature simpleFeature) {
        final RouteFragment fragment = new RouteFragment();
        fragment.setAct(act);
        fragment.setMapFragment(act.getMapFragment());
        fragment.setSimpleFeature(simpleFeature);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.route_widget, container, false);
        ButterKnife.inject(this, rootView);
        adapter = new RoutesAdapter(act, instructions);
        TextView destinationName = (TextView) rootView.findViewById(R.id.destination_name);
        destinationName.setText(simpleFeature.getProperty(NAME));
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
        sendFirstInstructionToGear();
        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                turnAutoPageOff();
                return false;
            }
        });
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
        final boolean voiceNavigationEnabled =
                PreferenceManager.getDefaultSharedPreferences(act)
                        .getBoolean(getString(R.string.settings_voice_navigation_key), true);

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

    @OnClick(R.id.resume_button)
    @SuppressWarnings("unused")
    public void onClickResume() {
        turnAutoPageOn();
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
        act.disableActionbar();
        act.hideActionBar();
        act.deactivateMapLocationUpdates();
        if (act.isInDebugMode()) {
            act.getDb().beginTransaction();
        }
        initProximityAlerts();
    }

    @Override
    public void onPause() {
        super.onPause();
        act.unregisterReceiver(locationReceiver);
        act.activateMapLocationUpdates();
        if (act.isInDebugMode()) {
            act.getDb().setTransactionSuccessful();
            act.getDb().endTransaction();
        }
        //clearRoute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act.enableActionbar();
        act.showActionBar();
        clearRoute();
    }

    public void createRouteTo(Location location) {
        mapFragment.clearMarkers();
        mapFragment.updateMap();
        isRouting = true;
        act.showProgressDialog();
        double[] loc1 = new double[] { location.getLatitude(), location.getLongitude() };
        double[] loc2 = new double[] { simpleFeature.getLat(), simpleFeature.getLon() };
        router.setLocation(loc1)
                .setLocation(loc2)
                .setZoomLevel(getMapController().getZoomLevel())
                .setDriving()
                .setCallback(this)
                .fetch();
    }

    public String getRouteId() {
        return routeId;
    }

    public int getWalkingAdvanceRadius() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
        return prefs.getInt(
                act.getString(R.string.settings_key_walking_advance_radius),
                act.getResources().getInteger(R.integer.route_advance_radius));
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        Logger.d("instructions: " + instructions.toString());
        this.instructions = instructions;
    }

    public SimpleFeature getSimpleFeature() {
        return simpleFeature;
    }

    public void setSimpleFeature(SimpleFeature simpleFeature) {
        this.simpleFeature = simpleFeature;
    }

    public GeoPoint getDestinationPoint() {
        return simpleFeature.getGeoPoint();
    }

    private void initProximityAlerts() {
        for (Instruction instruction : instructions) {
            proximityAlerts.add(instruction);
        }
    }

    private Location snapTo(Location location) {
        double[] onRoadPoint;
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        onRoadPoint = route.snapToRoute(
                new double[] { lat, lng }
        );

        if (onRoadPoint != null) {
            Location correctedLocation = new Location("Corrected");
            correctedLocation.setLatitude(onRoadPoint[0]);
            correctedLocation.setLongitude(onRoadPoint[1]);
            return correctedLocation;
        }
        return null;
    }

    private void manageMap(Location location, Location originalLocation) {
        if (location != null) {
            getMapController().setLocation(location).centerOn(location);
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
        if (!autoPaging || isRouting) {
            return;
        }
        Location correctedLocation = snapTo(location);
        if (correctedLocation == null) {
            createRouteTo(location);
            return;
        }
        storeLocationInfo(location, correctedLocation);
        manageMap(correctedLocation, location);
        StringBuilder debugStringBuilder = new StringBuilder();

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

    public boolean setRoute(Route route) {
        if (route.foundRoute()) {
            this.route = route;
            this.instructions = route.getRouteInstructions();
            getMapController().setMapPerspectiveForInstruction(instructions.get(0));
        } else {
            return false;
        }
        return true;
    }

    public boolean setRoute(JSONObject rawRoute) {
        storeRouteInDatabase(rawRoute);
        Route route = new Route(rawRoute);
        setRoute(route);
        return true;
    }

    private void storeRouteInDatabase(JSONObject rawRoute) {
        if (act.isInDebugMode()) {
            ContentValues insertValues = new ContentValues();
            routeId = UUID.randomUUID().toString();
            insertValues.put(COLUMN_TABLE_ID, routeId);
            insertValues.put(COLUMN_RAW, rawRoute.toString());
            act.getDb().insert(TABLE_ROUTES, null, insertValues);
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
            values.put(COLUMN_TABLE_ID, UUID.randomUUID().toString());
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
        sendToGear(i);
        if (previousPosition > i) {
            changeDistance(instructions.get(i + 1).getDistance());
        } else if (previousPosition < i) {
            changeDistance(-instructions.get(previousPosition).getDistance());
        }
        previousPosition = i;
        getMapController().setMapPerspectiveForInstruction(instructions.get(i));
        speakerbox.play(instructions.get(i).getFullInstruction());
    }

    @Override
    public void onPageScrollStateChanged(int i) {
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

    private void sendFirstInstructionToGear() {
        sendToGear(0);
    }

    private void sendToGear(int index) {
        try {
            GearServiceSocket conn = GearAgentService.getConnection();
            if (conn != null) {
                int channelId = GearAgentService.CHANNEL_ID;
                conn.send(channelId, instructions.get(index).getGearJson().toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void turnAutoPageOff() {
        if (autoPaging) {
            pagerPositionWhenPaused = pager.getCurrentItem();
        }
        autoPaging = false;
        resume.setVisibility(View.VISIBLE);
    }

    private void turnAutoPageOn() {
        pager.setCurrentItem(pagerPositionWhenPaused);
        resume.setVisibility(View.GONE);
        autoPaging = true;
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

    @Override
    public void success(Route route) {
        if (setRoute(route)) {
            act.dismissProgressDialog();
            isRouting = false;
            if (!isAdded()) {
                act.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.routes_container, this, TAG)
                        .commit();
            } else {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pager.setAdapter(new RoutesAdapter(act, instructions));
                    }
                });
            }
            drawRoute();
        } else {
            Toast.makeText(act,
                    act.getString(R.string.no_route_found), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void failure(int statusCode) {
        isRouting = false;
        onServerError(statusCode);
    }

}

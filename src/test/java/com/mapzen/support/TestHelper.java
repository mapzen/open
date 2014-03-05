package com.mapzen.support;

import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;
import com.mapzen.fragment.MapFragment;

import org.apache.commons.io.FileUtils;
import org.oscim.android.MapView;
import org.oscim.map.TestMap;
import org.robolectric.tester.android.view.TestMenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;

import java.io.File;

import static com.mapzen.entity.Feature.ADMIN1_ABBR;
import static com.mapzen.entity.Feature.ADMIN1_NAME;
import static com.mapzen.entity.Feature.NAME;
import static org.robolectric.Robolectric.buildActivity;

public final class TestHelper {

    public static final String MOCK_ROUTE_JSON = TestHelper.getFixture("basic_route");

    private TestHelper() {
    }

    public static TestBaseActivity initBaseActivity() {
        return initBaseActivityWithMenu(new TestMenu());
    }

    public static TestBaseActivity initBaseActivityWithMenu(TestMenu menu) {
        TestBaseActivity activity = buildActivity(TestBaseActivity.class)
                .create()
                .start()
                .resume()
                .visible()
                .get();
        activity.onCreateOptionsMenu(menu);
        activity.registerMapView(new MapView(activity));
        return activity;
    }

    public static MapFragment initMapFragment(BaseActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) manager.findFragmentById(R.id.map_fragment);
        mapFragment.setAct(activity);
        mapFragment.setMap(new TestMap());
        mapFragment.onStart();
        return mapFragment;
    }

    public static Location getTestLocation(String provider, float lat, float lng, long time) {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(time);
        return location;
    }

    public static Feature getTestFeature() {
        Feature feature = new Feature();
        feature.setLat(1.0);
        feature.setLon(1.0);
        feature.setProperty(NAME, "Test Feature");
        feature.setProperty(ADMIN1_NAME, "New York");
        feature.setProperty(ADMIN1_ABBR, "NY");
        return feature;
    }

    public static String getFixture(String name) {
        String basedir = System.getProperty("user.dir");
        File file = new File(basedir + "/src/test/fixtures/" + name + ".fixture");
        String fixture = "";
        try {
            fixture = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            fixture = "not found";
        }
        return fixture;
    }

    public static void enableDebugMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(context.getString(R.string.settings_key_debug), true);
        prefEditor.commit();
    }

    public static class TestLocation extends Location {
        private double distance;
        private double bearing;
        private double rLat, rLng;
        private Location originalLocation;
        private int R = 6371;

        public TestLocation(String provider) {
            super(provider);
        }

        private TestLocation(Builder builder) {
            super("fake tester");
            this.distance = builder.distance / R;
            this.bearing = Math.toRadians(builder.bearing);
            this.originalLocation = builder.originalLocation;
            this.rLat = Math.toRadians(originalLocation.getLatitude());
            this.rLng = Math.toRadians(originalLocation.getLongitude());
            this.setLatitude(Math.toDegrees(getFarAwayLat()));
            this.setLongitude(Math.toDegrees(getFarAwayLng()));
        }

        private double getFarAwayLat() {
            double lat = Math.asin(Math.sin(rLat)
                    * Math.cos(distance / R)
                    + Math.cos(rLat * Math.sin(distance / R))
                    * Math.cos(bearing));
            return lat;
        }

        private double getFarAwayLng() {
            double lng = rLng + Math.atan2(
                    Math.sin(bearing)
                            * Math.sin(distance)
                            * Math.cos(rLat)
                    ,
                    Math.cos(distance)
                            - Math.sin(rLat)
                            * Math.sin(getFarAwayLat())
            );

            return lng;
        }

        public static class Builder {
            private double distance;
            private double bearing;
            private Location originalLocation;

            public Builder(Location location) {
                this.originalLocation = location;
            }

            public Builder setDistance(double distance) {
                this.distance = distance;
                return this;
            }

            public Builder setBearing(double bearing) {
                this.bearing = bearing;
                return this;
            }

            public TestLocation build() {
                return new TestLocation(this);
            }
        }
    }
}

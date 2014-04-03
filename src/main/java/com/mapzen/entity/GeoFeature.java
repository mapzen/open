package com.mapzen.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import com.mapzen.android.gson.Feature;
import com.mapzen.geo.Geometry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class GeoFeature implements Parcelable {
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String COUNTRY_CODE = "country_code";
    public static final String COUNTRY_NAME = "country_name";
    public static final String ADMIN1_ABBR = "admin1_abbr";
    public static final String ADMIN1_NAME = "admin1_name";

    private HashMap<String, String> properties = new HashMap<String, String>();
    private Geometry geometry = new Geometry();

    @Override
    public String toString() {
        return "'" + getProperty(NAME) + "'[" + getLat() + ", " + getLon() + "]";
    }

    public MarkerItem getMarker() {
        GeoPoint geoPoint = new GeoPoint(getLat(), getLon());
        MarkerItem markerItem = new MarkerItem(this, getProperty(NAME),
                "Current Location", geoPoint);
        return markerItem;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(getLat(), getLon());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getLat());
        out.writeDouble(getLon());
        out.writeString(getProperty(NAME));
        out.writeString(getProperty(TYPE));
        out.writeString(getProperty(COUNTRY_CODE));
        out.writeString(getProperty(COUNTRY_NAME));
        out.writeString(getProperty(ADMIN1_ABBR));
        out.writeString(getProperty(ADMIN1_NAME));
    }

    public static GeoFeature readFromParcel(Parcel in) {
        GeoFeature geoFeature = new GeoFeature();
        geoFeature.setLat(in.readDouble());
        geoFeature.setLon(in.readDouble());
        geoFeature.setProperty(NAME, in.readString());
        geoFeature.setProperty(TYPE, in.readString());
        geoFeature.setProperty(COUNTRY_CODE, in.readString());
        geoFeature.setProperty(COUNTRY_NAME, in.readString());
        geoFeature.setProperty(ADMIN1_ABBR, in.readString());
        geoFeature.setProperty(ADMIN1_NAME, in.readString());
        return geoFeature;
    }

    public static final Parcelable.Creator<GeoFeature> CREATOR =
            new Parcelable.Creator<GeoFeature>() {
        @Override
        public GeoFeature[] newArray(int size) {
            return new GeoFeature[size];
        }

        public GeoFeature createFromParcel(Parcel in) {
            return GeoFeature.readFromParcel(in);
        }
    };

    @Override
    public boolean equals(Object o) {
        GeoFeature other = (GeoFeature) o;
        return getLat() == other.getLat()
                && getLon() == other.getLon()
                && getProperty(NAME).equals(other.getProperty(NAME));
    }

    public int hashCode() {
        return 0;
    }

    public static class ViewHolder {
        private TextView title;
        private TextView address;

        public void setTitle(TextView title) {
            this.title = title;
        }

        public void setAddress(TextView address) {
            this.address = address;
        }

        public void setFromFeature(GeoFeature geoFeature) {
            if (geoFeature != null) {
                title.setText(geoFeature.getProperty(NAME));
                address.setText(String.format(Locale.getDefault(), "%s, %s",
                        geoFeature.getProperty(ADMIN1_NAME), geoFeature.getProperty(ADMIN1_ABBR)));
            }
        }
    }

    public static GeoFeature fromFeature(Feature feature) {
        GeoFeature geoFeature = new GeoFeature();
        geoFeature.setProperty(NAME, feature.getProperties().getName());
        geoFeature.setProperty(ADMIN1_NAME, feature.getProperties().getAdmin1_name());
        geoFeature.setProperty(ADMIN1_ABBR, feature.getProperties().getAdmin1_abbr());
        geoFeature.setLon(feature.getGeometry().getCoordinates().get(0));
        geoFeature.setLat(feature.getGeometry().getCoordinates().get(1));
        return geoFeature;
    }

    public void buildFromJSON(JSONObject json) throws JSONException {
        JSONObject properties = json.getJSONObject("properties");
        Iterator<String> iterator = properties.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            setProperty(key, properties.getString(key));
        }
        JSONObject geometry = json.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        setLat(coordinates.getDouble(1));
        setLon(coordinates.getDouble(0));
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public double getLon() {
        return geometry.getLon();
    }

    public double getLat() {
        return geometry.getLat();
    }

    public void setLon(double lon) {
        geometry.setLon(lon);
    }

    public void setLat(double lat) {
        geometry.setLat(lat);
    }
}

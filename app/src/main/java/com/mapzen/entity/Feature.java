package com.mapzen.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mapzen.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class Feature extends com.mapzen.geo.Feature implements Parcelable {
    private static final String PELIAS_URL = "http://api-pelias-test.mapzen.com/";
    private static final String PELIAS_SUGGEST = "suggest";
    private static final String PELIAS_SEARCH = "search";
    private static final String PELIAS_SEARCH_URL = PELIAS_URL + PELIAS_SEARCH;
    private static final String PELIAS_SUGGEST_URL = PELIAS_URL + PELIAS_SUGGEST;
    public static final String TITLE = "title";
    public static final String PROPERTIES = "properties";
    public static final String GEOMETRY = "geometry";
    public static final String COORDINATES = "coordinates";
    public static final String DESCRIPTION = "description";
    public static final String COUNTRY_CODE = "country_code";
    public static final String COUNTRY_NAME = "country_name";
    public static final String ADMIN1_ABBR = "admin1_abbr";
    public static final String ADMIN1_NAME = "admin1_name";

    public Feature() {
    }

    public static JsonObjectRequest suggest(String query, Response.Listener successListener,
                                           Response.ErrorListener errorListener) {
        String url = String.format("%s?query=%s", PELIAS_SUGGEST_URL, Uri.encode(query));
        Log.v(LOG_TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                successListener, errorListener);
        return jsonObjectRequest;
    }

    public static JsonObjectRequest search(Map map, String query, Response.Listener successListener,
                              Response.ErrorListener errorListener) {
        BoundingBox boundingBox = map.getBoundingBox();
        String url = String.format("%s?query=%s&viewbox=%4f,%4f,%4f,%4f",
                PELIAS_SEARCH_URL, Uri.encode(query),
                boundingBox.getMinLongitude(), boundingBox.getMaxLatitude(),
                boundingBox.getMaxLongitude(), boundingBox.getMinLatitude());
        Log.v(LOG_TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                successListener, errorListener);
        return jsonObjectRequest;
    }

    @Override
    public String toString() {
        return "'" + getProperty(TITLE)  + "'[" + getLat() + getLon() + "]";
    }

    public static Feature fromJson(JSONObject obj) throws JSONException {
        Feature feature = new Feature();
        JSONObject properties = obj.getJSONObject(PROPERTIES);
        JSONObject geometry = obj.getJSONObject(GEOMETRY);
        JSONArray coordinates = geometry.getJSONArray(COORDINATES);
        feature.setLat(coordinates.getDouble(1));
        feature.setLon(coordinates.getDouble(0));
        String[] attributes = new String[] { TITLE, DESCRIPTION,
                COUNTRY_CODE, COUNTRY_NAME, ADMIN1_ABBR, ADMIN1_NAME};
        for (String attribute : attributes) {
            feature.setProperty(attribute, properties.getString(attribute));
        }
        return feature;
    }

    public MarkerItem getMarker() {
        GeoPoint geoPoint = new GeoPoint(getLat(), getLon());
        MarkerItem markerItem = new MarkerItem(getProperty(TITLE), "Current Location", geoPoint);
        markerItem.setMarkerHotspot(MarkerItem.HotspotPlace.TOP_CENTER);
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
        out.writeString(getProperty(TITLE));
        out.writeString(getProperty(DESCRIPTION));
        out.writeString(getProperty(COUNTRY_CODE));
        out.writeString(getProperty(COUNTRY_NAME));
        out.writeString(getProperty(ADMIN1_ABBR));
        out.writeString(getProperty(ADMIN1_NAME));
    }

    public static Feature readFromParcel(Parcel in) {
        Feature feature = new Feature();
        feature.setLat(in.readDouble());
        feature.setLon(in.readDouble());
        feature.setProperty(TITLE, in.readString());
        feature.setProperty(DESCRIPTION, in.readString());
        feature.setProperty(COUNTRY_CODE, in.readString());
        feature.setProperty(COUNTRY_NAME, in.readString());
        feature.setProperty(ADMIN1_ABBR, in.readString());
        feature.setProperty(ADMIN1_NAME, in.readString());
        return feature;
    }

    public static final Parcelable.Creator<Feature> CREATOR = new Parcelable.Creator<Feature>() {
        @Override
        public Feature[] newArray(int size) {
            return new Feature[size];
        }

        public Feature createFromParcel(Parcel in) {
            return Feature.readFromParcel(in);
        }
    };

    @Override
    public boolean equals(Object o) {
        Feature other = (Feature) o;
        return getLat() == other.getLat()
                && getLon() == other.getLon()
                && getProperty(TITLE).equals(other.getProperty(TITLE));
    }

    public int hashCode() {
        return 0;
    }

    static public class ViewHolder {
        public TextView title;
        public TextView address;

        public void setFromFeature(Feature feature) {
            if (feature != null) {
                title.setText(feature.getProperty(TITLE));
                address.setText(String.format("%s, %s", feature.getProperty(ADMIN1_NAME), feature.getProperty(ADMIN1_ABBR)));
            }
        }
    }
}

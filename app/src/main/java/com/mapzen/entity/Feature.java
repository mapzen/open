package com.mapzen.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class Feature implements Parcelable {
    private double lat;
    private double lon;
    private String displayName;
    private String description;
    private String countryCode;
    private String countryName;
    private String admin1Abbr;
    private String admin1Name;
    private static final String PELIAS_URL = "http://api-pelias-test.mapzen.com/";
    private static final String PELIAS_SUGGEST = "suggest";
    private static final String PELIAS_SEARCH = "search";
    private static final String PELIAS_SEARCH_URL = PELIAS_URL + PELIAS_SEARCH;
    private static final String PELIAS_SUGGEST_URL = PELIAS_URL + PELIAS_SUGGEST;

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
        return "'" + displayName + "'[" + lat + lon + "]";
    }

    public static Feature fromJson(JSONObject obj) throws JSONException {
        Feature feature = new Feature();
        JSONObject properties = obj.getJSONObject("properties");
        JSONObject geometry = obj.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        feature.setLat(coordinates.getDouble(1));
        feature.setLon(coordinates.getDouble(0));
        feature.setDisplayName(properties.getString("title"));
        feature.setDescription(properties.getString("description"));
        feature.setCountryCode(properties.getString("country_code"));
        feature.setCountryName(properties.getString("country_name"));
        feature.setAdmin1Abbr(properties.getString("admin1_abbr"));
        feature.setAdmin1Name(properties.getString("admin1_name"));
        return feature;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public String getDescription() {
        return description;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) { this.description = description; }

    public String getAdmin1Name() {
        return admin1Name;
    }

    public void setAdmin1Name(String admin1Name) {
        this.admin1Name = admin1Name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getAdmin1Abbr() {
        return admin1Abbr;
    }

    public void setAdmin1Abbr(String admin1Abbr) {
        this.admin1Abbr = admin1Abbr;
    }

    public MarkerItem getMarker() {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        MarkerItem markerItem = new MarkerItem(getDisplayName(), "Current Location", geoPoint);
        markerItem.setMarkerHotspot(MarkerItem.HotspotPlace.TOP_CENTER);
        return markerItem;
    }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(lat, lon);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(lat);
        out.writeDouble(lon);
        out.writeString(displayName);
        out.writeString(description);
        out.writeString(countryCode);
        out.writeString(countryName);
        out.writeString(admin1Abbr);
        out.writeString(admin1Name);
    }

    public static Feature readFromParcel(Parcel in) {
        Feature feature = new Feature();
        feature.setLat(in.readDouble());
        feature.setLon(in.readDouble());
        feature.setDisplayName(in.readString());
        feature.setDescription(in.readString());
        feature.setCountryCode(in.readString());
        feature.setCountryName(in.readString());
        feature.setAdmin1Abbr(in.readString());
        feature.setAdmin1Name(in.readString());
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
        return lat == other.getLat()
                && lon == other.getLon()
                && displayName.equals(other.getDisplayName());
    }

    public int hashCode() {
        return 0;
    }
}

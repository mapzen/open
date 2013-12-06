package com.mapzen.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import static com.mapzen.MapzenApplication.LOG_TAG;

public class Place implements Parcelable {
    private double lat;
    private double lon;
    private String displayName;
    private static final String PELIAS_URL = "http://api-pelias-test.mapzen.com/";
    private static final String PELIAS_SUGGEST = "suggest";
    private static final String PELIAS_SEARCH = "search";
    private static final String PELIAS_SEARCH_URL = PELIAS_URL + PELIAS_SEARCH;
    private static final String PELIAS_SUGGEST_URL = PELIAS_URL + PELIAS_SUGGEST;

    public Place() {
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

    public static Place fromJson(JSONObject obj) throws JSONException {
        Place place = new Place();
        JSONObject properties = obj.getJSONObject("properties");
        JSONObject geometry = obj.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        place.setLat(coordinates.getDouble(1));
        place.setLon(coordinates.getDouble(0));
        place.setDisplayName(properties.getString("name"));
        return place;
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

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MarkerItem getMarker() {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        MarkerItem markerItem = new MarkerItem(getDisplayName(), "Current Location", geoPoint);
        markerItem.setMarkerHotspot(MarkerItem.HotspotPlace.TOP_CENTER);
        return markerItem;
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
    }

    public static Place readFromParcel(Parcel in) {
        Place place = new Place();
        place.setLat(in.readDouble());
        place.setLon(in.readDouble());
        place.setDisplayName(in.readString());
        return place;
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }

        public Place createFromParcel(Parcel in) {
            return Place.readFromParcel(in);
        }
    };

    @Override
    public boolean equals(Object o) {
        Place other = (Place) o;
        return lat == other.getLat()
                && lon == other.getLon()
                && displayName.equals(other.getDisplayName());
    }

    public int hashCode() {
        return 0;
    }
}

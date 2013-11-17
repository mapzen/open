package com.mapzen.entity;

import android.os.Parcel;
import android.os.Parcelable;
import com.mapquest.android.maps.GeoPoint;
import org.json.JSONException;
import org.json.JSONObject;

public class Place implements Parcelable {
    private double lat;
    private double lon;
    private String displayName;

    public Place() {
    }

    public static Place fromJson(JSONObject obj) throws JSONException {
        Place place = new Place();
        place.setLat(obj.getDouble("lat"));
        place.setLon(obj.getDouble("lon"));
        place.setDisplayName(obj.getString("display_name"));
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

    public GeoPoint getPoint() {
        return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
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
        Place other = (Place)o;
        return lat == other.getLat()
                && lon == other.getLon()
                && displayName.equals(other.getDisplayName());
    }
}

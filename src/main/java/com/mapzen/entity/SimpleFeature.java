package com.mapzen.entity;

import com.mapzen.android.gson.Feature;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

public class SimpleFeature implements Parcelable {
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String COUNTRY_CODE = "country_code";
    public static final String COUNTRY_NAME = "country_name";
    public static final String ADMIN1_ABBR = "admin1_abbr";
    public static final String ADMIN0_ABBR = "admin0_abbr";
    public static final String ADMIN1_NAME = "admin1_name";
    public static final Parcelable.Creator<SimpleFeature> CREATOR =
            new Parcelable.Creator<SimpleFeature>() {
                @Override
                public SimpleFeature[] newArray(int size) {
                    return new SimpleFeature[size];
                }

                public SimpleFeature createFromParcel(Parcel in) {
                    return SimpleFeature.readFromParcel(in);
                }
            };
    private HashMap<String, String> properties = new HashMap<String, String>();
    private double lat, lon;
    private String hint;

    public static SimpleFeature readFromParcel(Parcel in) {
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setLat(in.readDouble());
        simpleFeature.setLon(in.readDouble());
        simpleFeature.setProperty(NAME, in.readString());
        simpleFeature.setProperty(TYPE, in.readString());
        simpleFeature.setProperty(COUNTRY_CODE, in.readString());
        simpleFeature.setProperty(COUNTRY_NAME, in.readString());
        simpleFeature.setProperty(ADMIN1_ABBR, in.readString());
        simpleFeature.setProperty(ADMIN1_NAME, in.readString());
        simpleFeature.setProperty(ADMIN0_ABBR, in.readString());
        simpleFeature.setHint(in.readString());
        return simpleFeature;
    }

    public static SimpleFeature fromFeature(Feature feature) {
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setProperty(NAME, feature.getProperties().getName());
        simpleFeature.setProperty(ADMIN1_NAME, feature.getProperties().getAdmin1_name());
        simpleFeature.setProperty(ADMIN1_ABBR, feature.getProperties().getAdmin1_abbr());
        simpleFeature.setProperty(ADMIN0_ABBR, feature.getProperties().getAdmin0_abbr());
        simpleFeature.setLon(feature.getGeometry().getCoordinates().get(0));
        simpleFeature.setLat(feature.getGeometry().getCoordinates().get(1));
        simpleFeature.setHint(feature.getProperties().getHint());
        return simpleFeature;
    }

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
        out.writeString(getProperty(ADMIN0_ABBR));
        out.writeString(getHint());
    }

    @Override
    public boolean equals(Object o) {
        SimpleFeature other = (SimpleFeature) o;
        return getLat() == other.getLat()
                && getLon() == other.getLon()
                && getHint() == other.getHint()
                && getProperty(NAME).equals(other.getProperty(NAME));
    }

    public int hashCode() {
        return 0;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getAbbr() {
        if (getProperty(ADMIN1_ABBR) != null) {
            return getProperty(ADMIN1_ABBR);
        } else {
            return getProperty(ADMIN0_ABBR);
        }
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

        public void setFromFeature(SimpleFeature simpleFeature) {
            if (simpleFeature != null) {
                title.setText(simpleFeature.getProperty(NAME));
                address.setText(String.format(Locale.getDefault(), "%s, %s",
                        simpleFeature.getProperty(ADMIN1_NAME),
                        simpleFeature.getAbbr()));
            }
        }
    }
}

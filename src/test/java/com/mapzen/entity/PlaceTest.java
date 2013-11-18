package com.mapzen.entity;

import android.os.Parcel;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PlaceTest {
    private Place place;
    private double expectedLat = Double.parseDouble("51.4993491");
    private double expectedLon = Double.parseDouble("-0.12739091");

    @Before
    public void setUp() throws Exception {
        JSONObject json = new JSONObject("{\"lat\":\"51.4993491\",\"lon\":\"-0.12739091\", " +
                "\"display_name\":\"Place Name to Display\"}");
        place = Place.fromJson(json);
    }

    @Test
    public void hasLatDouble() throws Exception {
        assertThat(expectedLat, is(place.getLat()));
    }

    @Test
    public void hasLonDouble() throws Exception {
        assertThat(expectedLon, is(place.getLon()));
    }

    @Test
    public void hasGeoPoint() throws Exception {
        GeoPoint expected = new GeoPoint(
                (int) (expectedLat * 1E6), (int) (expectedLon * 1E6));
        assertThat(expected, is(place.getPoint()));
    }

    @Test
    public void hasDisplayName() throws Exception {
        String expected = "Place Name to Display";
        assertThat(expected, is(place.getDisplayName()));
    }

    @Test
    public void isParcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        place.writeToParcel(parcel, 0);
        Place newPlace = Place.readFromParcel(parcel);
        assertThat(place, equalTo(newPlace));
    }
}

package com.mapzen.entity;

import android.os.Parcel;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.System;

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
        JSONObject json = new JSONObject("{\"" +
                "type\":\"Feature\"," +
                "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-0.12739091,51.4993491]}," +
                "\"properties\":{\"name\":\"Place Name to Display\"," +
                "\"type\":\"geoname\",\"marker-color\":\"#F00\"}}");
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
    public void hasDisplayName() throws Exception {
        String expected = "Place Name to Display";
        assertThat(expected, is(place.getDisplayName()));
    }

    @Test
    public void isParcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        place.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Place newPlace = Place.readFromParcel(parcel);
        assertThat(place, equalTo(newPlace));
    }
}

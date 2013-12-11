package com.mapzen.entity;

import android.os.Parcel;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FeatureTest {
    private Feature feature;
    private double expectedLat = Double.parseDouble("51.4993491");
    private double expectedLon = Double.parseDouble("-0.12739091");

    @Before
    public void setUp() throws Exception {
        JSONObject json = new JSONObject("{\"" +
                "type\":\"Feature\"," +
                "\"geometry\":" +
                    "{\"type\":\"Point\"," +
                              "\"coordinates\": [-0.12739091,51.4993491]}," +
                "\"properties\":" +
                    "{\"title\":\"Feature Name to Display\"," +
                    "\"description\":\"testDescription\"," +
                    "\"country_code\":\"testUS\"," +
                    "\"country_name\":\"testUnited States\"," +
                    "\"admin1_abbr\":\"testNY\"," +
                    "\"admin1_name\":\"testNew York\"," +
                    "\"type\":\"geoname\"," +
                    "\"marker-color\":\"#F00\"}}");
        feature = Feature.fromJson(json);
    }

    @Test
    public void hasLatDouble() throws Exception {
        assertThat(feature.getLat(), is(expectedLat));
    }

    @Test
    public void hasLonDouble() throws Exception {
        assertThat(feature.getLon(), is(expectedLon));
    }

    @Test
    public void hasDescription() throws Exception {
        String expectedDescription = "testDescription";
        assertThat(feature.getDescription(), is(expectedDescription));
    }

    @Test
    public void hasCountryCode() throws Exception {
        String expectedCountryCode = "testUS";
        assertThat(feature.getCountryCode(), is(expectedCountryCode));
    }
    @Test
    public void hasCountryName() throws Exception {
        String expectedCountryName = "testUnited States";
        assertThat(feature.getCountryName(), is(expectedCountryName));
    }
    @Test
    public void hasAdmin1Abbr() throws Exception {
        String expectedAdmin1Abbr = "testNY";
        assertThat(feature.getAdmin1Abbr(), is(expectedAdmin1Abbr));
    }
    @Test
    public void hasAdmin1Name() throws Exception {
        String expectedAdmin1Name = "testNew York";
        assertThat(feature.getAdmin1Name(), is(expectedAdmin1Name));
    }

    @Test
    public void hasDisplayName() throws Exception {
        String expected = "Feature Name to Display";
        assertThat(expected, is(feature.getDisplayName()));
    }

    @Test
    public void isParcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        feature.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Feature newFeature = Feature.readFromParcel(parcel);
        assertThat(feature, equalTo(newFeature));
    }
}

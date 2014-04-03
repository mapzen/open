package com.mapzen.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SimpleFeatureTest {
    private SimpleFeature simpleFeature = new SimpleFeature();
    private double expectedLat = Double.parseDouble("51.4993491");
    private double expectedLon = Double.parseDouble("-0.12739091");

    @Before
    public void setUp() throws Exception {
        simpleFeature = getTestSimpleFeature();
        simpleFeature.setLat(expectedLat);
        simpleFeature.setLon(expectedLon);
    }

    @Test
    public void hasLatDouble() throws Exception {
        assertThat(simpleFeature.getLat(), is(expectedLat));
    }

    @Test
    public void hasLonDouble() throws Exception {
        assertThat(simpleFeature.getLon(), is(expectedLon));
    }

    @Test
    public void isParcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        simpleFeature.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SimpleFeature newSimpleFeature = SimpleFeature.readFromParcel(parcel);
        assertThat(simpleFeature, equalTo(newSimpleFeature));
    }
}

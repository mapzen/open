package com.mapzen.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SimpleFeatureTest {
    SimpleFeature simpleFeature = new SimpleFeature();
    double expectedLat = Double.parseDouble("51.4993491");
    double expectedLon = Double.parseDouble("-0.12739091");
    String expectedHint = "expected hint";

    @Before
    public void setUp() throws Exception {
        simpleFeature = getTestSimpleFeature();
        simpleFeature.setLat(expectedLat);
        simpleFeature.setLon(expectedLon);
        simpleFeature.setHint(expectedHint);
    }

    @Test
    public void hasLatDouble() throws Exception {
        assertThat(simpleFeature.getLat()).isEqualTo(expectedLat);
    }

    @Test
    public void hasLonDouble() throws Exception {
        assertThat(simpleFeature.getLon()).isEqualTo(expectedLon);
    }

    @Test
    public void hasHint() throws Exception {
        assertThat(simpleFeature.getHint()).isEqualTo(expectedHint);
    }

    @Test
    public void isParcelable() throws Exception {
        Parcel parcel = Parcel.obtain();
        simpleFeature.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        SimpleFeature newSimpleFeature = SimpleFeature.readFromParcel(parcel);
        assertThat(simpleFeature).isEqualTo(newSimpleFeature);
    }
}

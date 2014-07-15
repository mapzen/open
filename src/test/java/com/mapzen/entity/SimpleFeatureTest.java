package com.mapzen.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import android.widget.TextView;

import static com.mapzen.entity.SimpleFeature.ADMIN0_ABBR;
import static com.mapzen.entity.SimpleFeature.ADMIN1_ABBR;
import static com.mapzen.entity.SimpleFeature.ADMIN1_NAME;
import static com.mapzen.entity.SimpleFeature.LOCAL_ADMIN;
import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SimpleFeatureTest {
    SimpleFeature simpleFeature = new SimpleFeature();
    double expectedLat = Double.parseDouble("51.4993491");
    double expectedLon = Double.parseDouble("-0.12739091");
    String expectedHint = "expected hint";
    String expectedTitle = "expected title";
    String expectedAdmin1Abbr = "expected admin1 abbr";
    String expectedAdmin0Abbr = "expected admin0 abbr";
    String expectedAdmin1Name = "expected name";
    String expectedLocality = "expected locality";

    @Before
    public void setUp() throws Exception {
        simpleFeature = getTestSimpleFeature();
        simpleFeature.setLat(expectedLat);
        simpleFeature.setLon(expectedLon);
        simpleFeature.setHint(expectedHint);
        simpleFeature.setProperty(NAME, expectedTitle);
        simpleFeature.setProperty(ADMIN1_ABBR, expectedAdmin1Abbr);
        simpleFeature.setProperty(ADMIN1_NAME, expectedAdmin1Name);
        simpleFeature.setProperty(ADMIN0_ABBR, expectedAdmin0Abbr);
        simpleFeature.setProperty(LOCAL_ADMIN, expectedLocality);
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

    @Test
    public void ViewHolder_setFromFeature_shouldSetTitle() throws Exception {
        TextView title = new TextView(application.getBaseContext());
        TextView address = new TextView(application.getBaseContext());
        SimpleFeature.ViewHolder holder =  new SimpleFeature.ViewHolder();
        holder.setTitle(title);
        holder.setAddress(address);
        holder.setFromFeature(simpleFeature);
        assertThat(title.getText().toString()).isEqualTo(simpleFeature.getProperty(NAME));
    }

    @Test
    public void ViewHolder_setFromFeature_shouldSetAddress() throws Exception {
        TextView title = new TextView(application.getBaseContext());
        TextView address = new TextView(application.getBaseContext());
        SimpleFeature.ViewHolder holder =  new SimpleFeature.ViewHolder();
        holder.setTitle(title);
        holder.setAddress(address);
        holder.setFromFeature(simpleFeature);
        assertThat(address.getText().toString()).contains(expectedAdmin1Abbr);
        assertThat(address.getText().toString()).contains(expectedLocality);
    }

    @Test
    public void ViewHolder_setFromFeature_shouldFallbacktoAdmin0() throws Exception {
        TextView title = new TextView(application.getBaseContext());
        TextView address = new TextView(application.getBaseContext());
        SimpleFeature.ViewHolder holder =  new SimpleFeature.ViewHolder();
        holder.setTitle(title);
        holder.setAddress(address);
        simpleFeature.setProperty(ADMIN1_ABBR, null);
        holder.setFromFeature(simpleFeature);
        assertThat(address.getText().toString()).doesNotContain("null");
        assertThat(address.getText().toString()).contains(expectedAdmin0Abbr);
        assertThat(address.getText().toString()).contains(expectedLocality);
    }

    public void getAbbr_shouldReturnAdmin1() throws Exception {
       assertThat(simpleFeature.getAbbr()).isEqualTo(simpleFeature.getProperty(ADMIN1_ABBR));
    }

    public void getAbbr_shouldReturnAdmin0() throws Exception {
        simpleFeature.setProperty(ADMIN1_ABBR, null);
        assertThat(simpleFeature.getAbbr()).isEqualTo(simpleFeature.getProperty(ADMIN0_ABBR));
    }
}

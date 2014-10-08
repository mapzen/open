package com.mapzen.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import android.widget.TextView;

import static com.mapzen.entity.SimpleFeature.ADMIN1_ABBR;
import static com.mapzen.entity.SimpleFeature.ADMIN1;
import static com.mapzen.entity.SimpleFeature.ADMIN2;
import static com.mapzen.entity.SimpleFeature.ALPHA3;
import static com.mapzen.entity.SimpleFeature.LOCALITY;
import static com.mapzen.entity.SimpleFeature.LOCAL_ADMIN;
import static com.mapzen.entity.SimpleFeature.NEIGHBORHOOD;
import static com.mapzen.entity.SimpleFeature.TEXT;
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
        simpleFeature.setProperty(TEXT, expectedTitle);
        simpleFeature.setProperty(ADMIN1_ABBR, expectedAdmin1Abbr);
        simpleFeature.setProperty(ADMIN1, expectedAdmin1Name);
        simpleFeature.setProperty(ALPHA3, expectedAdmin0Abbr);
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
        assertThat(title.getText().toString()).isEqualTo(simpleFeature.getProperty(TEXT));
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
        assertThat(address.getText().toString()).contains(simpleFeature.getCity());
        assertThat(address.getText().toString()).contains(simpleFeature.getAdmin());
    }

    @Test
    public void getSingleLine_shouldReturnNameLocalityAndAbbreviation() throws Exception {
        assertThat(simpleFeature.getSingleLine()).isEqualTo(expectedTitle + ", " + expectedLocality
                + ", " + simpleFeature.getAdmin());
    }

    @Test
    public void getAdmin_shouldReturnBlankStringIfNoUsefulPropertyFound() throws Exception {
        simpleFeature.setProperty(ADMIN1_ABBR, null);
        simpleFeature.setProperty(ADMIN1, null);
        simpleFeature.setProperty(ALPHA3, null);
        assertThat(simpleFeature.getAdmin()).isEmpty();
    }

    @Test
    public void getAdmin_shouldPreferAdmin1Abbr() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(ADMIN1_ABBR, expected);
        simpleFeature.setProperty(ADMIN1, "bla");
        simpleFeature.setProperty(ALPHA3, "bla");
        assertThat(simpleFeature.getAdmin()).isEqualTo(expected);
    }

    @Test
    public void getAdmin_shouldPickAdmin1IfAbbrIsNotAvailable() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(ADMIN1_ABBR, null);
        simpleFeature.setProperty(ADMIN1, expected);
        simpleFeature.setProperty(ALPHA3, "bla");
        assertThat(simpleFeature.getAdmin()).isEqualTo(expected);
    }

    @Test
    public void getAdmin_shouldPickAlpha3IfAsLastOption() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(ADMIN1_ABBR, null);
        simpleFeature.setProperty(ADMIN1, null);
        simpleFeature.setProperty(ALPHA3, expected);
        assertThat(simpleFeature.getAdmin()).isEqualTo(expected);
    }

    @Test
    public void getCity_shouldReturnBlankStringIfNoUsefulPropertyFound() throws Exception {
        simpleFeature.setProperty(LOCAL_ADMIN, null);
        simpleFeature.setProperty(LOCALITY, null);
        simpleFeature.setProperty(NEIGHBORHOOD, null);
        simpleFeature.setProperty(ADMIN2, null);
        assertThat(simpleFeature.getCity()).isEmpty();
    }

    @Test
    public void getCity_shouldPreferLocalAdmin() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(LOCAL_ADMIN, expected);
        simpleFeature.setProperty(LOCALITY, "bla");
        simpleFeature.setProperty(NEIGHBORHOOD, "bla");
        simpleFeature.setProperty(ADMIN2, "bla");
        assertThat(simpleFeature.getCity()).isEqualTo(expected);
    }

    @Test
    public void getCity_shouldFallBackToLocality() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(LOCAL_ADMIN, null);
        simpleFeature.setProperty(LOCALITY, expected);
        simpleFeature.setProperty(NEIGHBORHOOD, "bla");
        simpleFeature.setProperty(ADMIN2, "bla");
        assertThat(simpleFeature.getCity()).isEqualTo(expected);
    }

    @Test
    public void getCity_shouldFallBackToAdmin2() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(LOCAL_ADMIN, null);
        simpleFeature.setProperty(LOCALITY, null);
        simpleFeature.setProperty(NEIGHBORHOOD, null);
        simpleFeature.setProperty(ADMIN2, expected);
        assertThat(simpleFeature.getCity()).isEqualTo(expected);
    }

    @Test
    public void getCity_shouldFallBackToNeighborhood() throws Exception {
        String expected = "expected";
        simpleFeature.setProperty(LOCAL_ADMIN, null);
        simpleFeature.setProperty(LOCALITY, null);
        simpleFeature.setProperty(NEIGHBORHOOD, expected);
        simpleFeature.setProperty(ADMIN2, "bla");
        assertThat(simpleFeature.getCity()).isEqualTo(expected);
    }
}

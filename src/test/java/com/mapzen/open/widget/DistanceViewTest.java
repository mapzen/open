package com.mapzen.open.widget;

import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class DistanceViewTest {
    private DistanceView view;

    @Before
    public void setUp() throws Exception {
        view = new DistanceView(Robolectric.application);
    }

    @Test
    public void getDistance_shouldBeUnformatted() throws Exception {
        view.setDistance(100);
        assertThat(view.getDistance()).isEqualTo(100);
    }

    @Test
    public void setFormattedDistance_shouldBeFormattedDefault() throws Exception {
        view.setDistance(100);
        assertThat(view.getText()).isEqualTo(DistanceFormatter.format(100));
    }

    @Test
    public void setFormattedDistance_shouldBeFormattedRealTime() throws Exception {
        view.setRealTime(true);
        view.setDistance(100);
        assertThat(view.getText()).isEqualTo(DistanceFormatter.format(100, true));
    }

    @Test
    public void setFormattedDistance_shouldBeFormattedWithOutRealTime() throws Exception {
        view.setRealTime(false);
        view.setDistance(100);
        assertThat(view.getText()).isEqualTo(DistanceFormatter.format(100, false));
    }
}

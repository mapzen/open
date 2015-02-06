package com.mapzen.open.util;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;

import static com.mapzen.open.util.DisplayHelper.IconStyle;
import static com.mapzen.open.util.DisplayHelper.getRouteDrawable;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MapzenTestRunner.class)
public class DisplayHelperTest {
    @Test
    public void shouldReturnGrayTurnIcon() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 2, IconStyle.GRAY))
                .isEqualTo(R.drawable.ic_route_gr_2);
    }

    @Test
    public void shouldReturnCurrentTurnIcon() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 2, IconStyle.STANDARD))
                .isEqualTo(R.drawable.ic_route_2);
    }

    @Test
    public void shouldReturnDefaultIconIfNoneFound() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 99, IconStyle.STANDARD))
                .isEqualTo(R.drawable.ic_route_1);
    }

    @Test
    public void shouldReturnGrayDefaultIconIfNoneFound() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 99, IconStyle.GRAY))
                .isEqualTo(R.drawable.ic_route_gr_1);
    }
}

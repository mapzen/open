package com.mapzen.util;

import android.app.Activity;
import com.mapzen.R;
import com.mapzen.support.MapzenTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static com.mapzen.util.DisplayHelper.getRouteDrawable;
import static com.mapzen.util.DisplayHelper.IconStyle;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
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


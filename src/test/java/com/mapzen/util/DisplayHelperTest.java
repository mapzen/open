package com.mapzen.util;

import android.app.Activity;
import com.mapzen.R;
import com.mapzen.support.MapzenTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static com.mapzen.util.DisplayHelper.IconStyle;
import static com.mapzen.util.DisplayHelper.getRouteDrawable;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DisplayHelperTest {
    @Test
    public void shouldReturnWhiteTurnIcon() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 2, IconStyle.WHITE))
                .isEqualTo(R.drawable.ic_route_wh_2);
    }

    @Test
    public void shouldReturnBlackTurnIcon() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 2, IconStyle.BLACK))
                .isEqualTo(R.drawable.ic_route_bl_2);
    }

    @Test
    public void shouldReturnDefaultWhiteIconIfNoneFound() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 99, IconStyle.WHITE))
                .isEqualTo(R.drawable.ic_route_wh_10);
    }

    @Test
    public void shouldReturnDefaultBlackIconIfNoneFound() throws Exception {
        assertThat(getRouteDrawable(new Activity(), 99, IconStyle.BLACK))
                .isEqualTo(R.drawable.ic_route_bl_10);
    }
}
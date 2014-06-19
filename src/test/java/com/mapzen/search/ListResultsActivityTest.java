package com.mapzen.search;

import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.getShadowApplication;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ListResultsActivityTest {
    private ListResultsActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = buildActivity(ListResultsActivity.class)
                .create()
                .start()
                .resume()
                .visible()
                .get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldHaveTitle() throws Exception {
        assertThat(getShadowApplication().getAppManifest()
                .getActivityLabel(ListResultsActivity.class)).isEqualTo("@string/results_title");
    }
}

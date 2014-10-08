package com.mapzen.open.widget;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class EditIntPreferenceTest {
    private EditIntPreference pref;

    @Before
    public void setUp() throws Exception {
        pref = new EditIntPreference(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(pref).isNotNull();
    }

    @Test
    public void persistString_shouldHandleExceptionIfUnableToParseIntegerValue() throws Exception {
        pref.persistString("blah");
    }
}

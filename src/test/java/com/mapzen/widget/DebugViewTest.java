package com.mapzen.widget;

import com.mapzen.R;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.widget.TextView;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugViewTest {
    DebugView debugView;

    @Before
    public void setUp() throws Exception {
        debugView = new DebugView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(debugView).isNotNull();
    }

    @Test
    public void shouldSetText() throws Exception {
        debugView.setText("test");
        assertThat((TextView) debugView.findViewById(R.id.text)).hasText("test");
    }

    @Test
    public void shouldGetText() throws Exception {
        debugView.setText("test");
        assertThat(debugView.getText()).isEqualTo("test");
    }
}

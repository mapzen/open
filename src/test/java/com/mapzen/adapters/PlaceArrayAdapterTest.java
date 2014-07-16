package com.mapzen.adapters;

import com.mapzen.TestMapzenApplication;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.graphics.Typeface;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PlaceArrayAdapterTest {
    private PlaceArrayAdapter adapter;
    private ArrayList<SimpleFeature> features;
    private TextView textView;
    @Inject Typeface typeface;

    @Before
    public void setUp() throws Exception {
        features = new ArrayList<SimpleFeature>();
        features.add(TestHelper.getTestSimpleFeature());
        adapter = new PlaceArrayAdapter(application, features);
        textView = (TextView) adapter.getView(0, null, null);
        ((TestMapzenApplication) application).inject(this);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }

    @Test
    public void shouldDisplaySingleLineSummary() throws Exception {
        assertThat(textView).hasText(features.get(0).getSingleLine());
    }

    @Test
    public void shouldHaveCustomTypeface() throws Exception {
        assertThat(textView).hasTypeface(typeface);
    }
}

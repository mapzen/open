package com.mapzen.open.adapters;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper.RoutePreviewSubscriber;

import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(MapzenTestRunner.class)
public class SearchViewAdapterTest {
    private static final Activity ACTIVITY = buildActivity(Activity.class).create().get();

    private SearchViewAdapter adapter;
    private ArrayList<SimpleFeature> features;

    @Inject Bus bus;

    @Before
    public void setUp() throws Exception {
        ((MapzenApplication) application).inject(this);
        features = new ArrayList<SimpleFeature>();
        features.add(getTestSimpleFeature());
        features.add(getTestSimpleFeature());
        features.add(getTestSimpleFeature());
        adapter = new SearchViewAdapter(ACTIVITY, features);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }

    @Test
    public void shouldCreateItem() throws Exception {
        View view = (View) adapter.instantiateItem(new FrameLayout(ACTIVITY), 0);
        assertThat(view).isNotNull();
    }

    @Test
    public void shouldDisplayTitle() throws Exception {
        View view = (View) adapter.instantiateItem(new FrameLayout(ACTIVITY), 0);
        TextView title = (TextView) view.findViewById(R.id.title);
        assertThat(title).hasText(getTestSimpleFeature().getTitle());
    }

    @Test
    public void shouldDisplayAddress() throws Exception {
        View view = (View) adapter.instantiateItem(new FrameLayout(ACTIVITY), 0);
        TextView address = (TextView) view.findViewById(R.id.address);
        assertThat(address).hasText(getTestSimpleFeature().getAddress());
    }

    @Test
    public void shouldPostRoutePreviewEvent() throws Exception {
        View view = (View) adapter.instantiateItem(new FrameLayout(ACTIVITY), 0);
        TextView start = (TextView) view.findViewById(R.id.start);
        RoutePreviewSubscriber subscriber = new RoutePreviewSubscriber();
        bus.register(subscriber);
        start.performClick();
        assertThat(subscriber.getEvent().getSimpleFeature())
                .isEqualsToByComparingFields(getTestSimpleFeature());
    }
}

package com.mapzen.open.search;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(MapzenTestRunner.class)
public class AutoCompleteListViewTest {
    private static final Activity ACTIVITY = buildActivity(Activity.class).create().get();

    private AutoCompleteListView autoCompleteListView;

    @Before
    public void setUp() throws Exception {
        autoCompleteListView = new AutoCompleteListView(ACTIVITY);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(autoCompleteListView).isNotNull();
    }

    @Test
    public void showHeader_shouldAddHeaderView() throws Exception {
        autoCompleteListView.showHeader();
        assertThat(autoCompleteListView).hasHeaderViewsCount(1);
    }

    @Test
    public void showHeader_shouldNotAddMultipleHeaderViews() throws Exception {
        autoCompleteListView.showHeader();
        autoCompleteListView.showHeader();
        assertThat(autoCompleteListView).hasHeaderViewsCount(1);
    }

    @Test
    public void hideHeader_shouldRemoveHeaderView() throws Exception {
        autoCompleteListView.showHeader();
        autoCompleteListView.hideHeader();
        assertThat(autoCompleteListView).hasHeaderViewsCount(0);
    }
}

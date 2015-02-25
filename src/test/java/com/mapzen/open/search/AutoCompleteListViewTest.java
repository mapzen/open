package com.mapzen.open.search;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
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
    public void shouldHaveHeaderView() throws Exception {
        assertThat(autoCompleteListView).hasHeaderViewsCount(1);
    }

    @Test
    public void showHeader_shouldAddHeaderView() throws Exception {
        autoCompleteListView.hideHeader();
        autoCompleteListView.showHeader();
        assertThat(autoCompleteListView.findViewById(R.id.recent_search_header)).isVisible();
    }

    @Test
    public void hideHeader_shouldRemoveHeaderView() throws Exception {
        autoCompleteListView.showHeader();
        autoCompleteListView.hideHeader();
        assertThat(autoCompleteListView.findViewById(R.id.recent_search_header)).isGone();
    }

    @Test
    public void isHeaderVisible_shouldReportHeaderVisibility() throws Exception {
        autoCompleteListView.showHeader();
        assertThat(autoCompleteListView.isHeaderVisible()).isTrue();

        autoCompleteListView.hideHeader();
        assertThat(autoCompleteListView.isHeaderVisible()).isFalse();
    }
}

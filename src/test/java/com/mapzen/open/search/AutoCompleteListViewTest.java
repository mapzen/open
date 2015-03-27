package com.mapzen.open.search;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;

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

    @Test
    public void setAdapter_shouldHideEmptyViewWhenItemIsAddedToAdapter() throws Exception {
        ArrayAdapter adapter = new ArrayAdapter(ACTIVITY, 0);
        View empty = new View(ACTIVITY);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setAdapter(adapter);
        autoCompleteListView.setVisibility(View.VISIBLE);
        adapter.add(new Object());
        assertThat(empty).isGone();
    }

    @Test
    public void setAdapter_shouldShowEmptyViewWhenItemIsRemovedFromAdapter() throws Exception {
        ArrayAdapter adapter = new ArrayAdapter(ACTIVITY, 0);
        adapter.add(new Object());
        View empty = new View(ACTIVITY);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setAdapter(adapter);
        autoCompleteListView.setVisibility(View.VISIBLE);
        adapter.clear();
        assertThat(empty).isVisible();
    }

    @Test
    public void setAdapter_shouldHideEmptyViewWhenItemIsRemovedIfListViewHidden() throws Exception {
        ArrayAdapter adapter = new ArrayAdapter(ACTIVITY, 0);
        adapter.add(new Object());
        View empty = new View(ACTIVITY);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setAdapter(adapter);
        autoCompleteListView.setVisibility(View.GONE);
        adapter.clear();
        assertThat(empty).isGone();
    }

    @Test
    public void setVisibility_GONE_shouldHideEmptyView() throws Exception {
        View empty = new View(ACTIVITY);
        empty.setVisibility(View.VISIBLE);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setVisibility(View.GONE);
        assertThat(empty).isGone();
    }

    @Test
    public void setVisibility_INVISIBLE_shouldHideEmptyView() throws Exception {
        View empty = new View(ACTIVITY);
        empty.setVisibility(View.VISIBLE);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setVisibility(View.INVISIBLE);
        assertThat(empty).isGone();
    }

    @Test
    public void setVisibility_VISIBLE_shouldShowEmptyViewIfAdapterIsEmpty() throws Exception {
        ArrayAdapter adapter = new ArrayAdapter(ACTIVITY, 0);
        autoCompleteListView.setAdapter(adapter);

        View empty = new View(ACTIVITY);
        empty.setVisibility(View.GONE);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setVisibility(View.VISIBLE);
        assertThat(empty).isVisible();
    }

    @Test
    public void setVisibility_VISIBILE_shouldHideEmptyViewIfAdapterIsNotEmpty() throws Exception {
        ArrayAdapter adapter = new ArrayAdapter(ACTIVITY, 0);
        adapter.add(new Object());
        autoCompleteListView.setAdapter(adapter);

        View empty = new View(ACTIVITY);
        empty.setVisibility(View.VISIBLE);
        autoCompleteListView.setEmptyView(empty);
        autoCompleteListView.setVisibility(View.VISIBLE);
        assertThat(empty).isGone();
    }
}

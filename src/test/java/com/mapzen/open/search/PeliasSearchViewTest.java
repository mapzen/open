package com.mapzen.open.search;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.app.Activity;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PeliasSearchViewTest {
    private static final Activity ACTIVITY = buildActivity(Activity.class).create().get();

    private PeliasSearchView peliasSearchView;

    @Before
    public void setUp() throws Exception {
        peliasSearchView = new PeliasSearchView(ACTIVITY);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(peliasSearchView).isNotNull();
    }

    @Test
    public void setAutoCompleteListView_shouldShowListViewWhenQueryGetsFocus() throws Exception {
        ListView listView = new ListView(ACTIVITY);
        listView.setVisibility(GONE);
        peliasSearchView.setAutoCompleteListView(listView);
        AutoCompleteTextView queryText = getQueryTextView();
        shadowOf(queryText).setViewFocus(true);
        assertThat(listView).isVisible();
    }

    @Test
    public void setAutoCompleteListView_shouldHideListViewWhenQueryLosesFocus() throws Exception {
        ListView listView = new ListView(ACTIVITY);
        listView.setVisibility(VISIBLE);
        peliasSearchView.setAutoCompleteListView(listView);
        AutoCompleteTextView queryText = getQueryTextView();
        shadowOf(queryText).setViewFocus(false);
        assertThat(listView).isGone();
    }

    private AutoCompleteTextView getQueryTextView() {
        LinearLayout linearLayout1 = (LinearLayout) peliasSearchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        return (AutoCompleteTextView) linearLayout3.getChildAt(0);
    }
}

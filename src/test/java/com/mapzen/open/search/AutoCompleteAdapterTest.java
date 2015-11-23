package com.mapzen.open.search;

import com.mapzen.pelias.Pelias;
import com.mapzen.pelias.gson.Result;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.pelias.SimpleFeature;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper;
import com.mapzen.open.util.ParcelableUtil;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscim.core.MapPosition;
import org.robolectric.tester.android.database.TestCursor;
import org.robolectric.tester.android.view.TestMenu;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import javax.inject.Inject;

import retrofit.Callback;

import static com.mapzen.open.MapController.getMapController;
import static com.mapzen.pelias.SimpleFeature.TEXT;
import static com.mapzen.open.support.TestHelper.assertSpan;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initMapFragment;
import static com.mapzen.open.util.MixpanelHelper.Event.PELIAS_SUGGEST;
import static com.mapzen.open.util.MixpanelHelper.Payload.PELIAS_TERM;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(MapzenTestRunner.class)
public class AutoCompleteAdapterTest {
    private AutoCompleteAdapter adapter;
    private BaseActivity baseActivity;
    private TextView view;
    private FragmentManager fragmentManager;
    private SimpleFeature simpleFeature;
    private TestMenu menu;
    @Inject Pelias pelias;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject SavedSearch savedSearch;
    @Inject MapzenApplication app;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback<Result>> callback;

    @Before
    public void setUp() throws Exception {
        ((MapzenApplication) application).inject(this);
        MockitoAnnotations.initMocks(this);
        savedSearch.clear();
        menu = new TestMenu();
        baseActivity = TestHelper.initBaseActivityWithMenu(menu);
        fragmentManager = baseActivity.getSupportFragmentManager();
        adapter = new AutoCompleteAdapter(baseActivity.getSupportActionBar().getThemedContext(),
                baseActivity, ((MapzenApplication) application).getColumns(), fragmentManager);
        adapter.setSearchView(baseActivity.getSearchView());
        adapter.setMapFragment(initMapFragment(baseActivity));
        view = (TextView) adapter.newView(baseActivity,
                new TestCursor(), new FrameLayout(baseActivity));
        simpleFeature = getTestSimpleFeature();
        view.setTag(simpleFeature);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(adapter).isNotNull();
    }

    @Test
    public void onClick_shouldCreatePagerResultsFragment() throws Exception {
        view.performClick();
        assertThat(fragmentManager).hasFragmentWithTag(PagerResultsFragment.TAG);
    }

    @Test
    public void onClick_shouldSaveTerm() throws Exception {
        view.setText("saved term");
        view.performClick();
        assertThat(savedSearch.getIterator().next().getTerm()).isEqualTo("saved term");
    }

    @Test
    public void onClick_shouldReplaceExistingPagerResultsFragment() throws Exception {
        PagerResultsFragment fragment1 = PagerResultsFragment.newInstance(baseActivity);
        fragmentManager.beginTransaction().add(R.id.pager_results_container, fragment1,
                PagerResultsFragment.TAG).commit();

        view.performClick();
        PagerResultsFragment fragment2 = (PagerResultsFragment) fragmentManager
                .findFragmentByTag(PagerResultsFragment.TAG);

        assertThat(fragment1).isNotAdded();
        assertThat(fragment2).isAdded();
        assertThat(fragment1).isNotSameAs(fragment2);
    }

    @Test
    public void onClick_shouldAddFeatureToPagerResultsFragment() throws Exception {
        view.performClick();
        PagerResultsFragment pagerResultsFragment = (PagerResultsFragment)
                fragmentManager.findFragmentByTag(PagerResultsFragment.TAG);
        assertThat(pagerResultsFragment.pager.getAdapter().getCount()).isEqualTo(1);
    }

    @Test
    public void onQueryTextChange_shouldShowSavedSearches() throws Exception {
        savedSearch.store("saved query 1");
        adapter.onQueryTextChange("");
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        assertThat(cursor.getString(1)).isEqualTo("saved query 1");
    }

    @Test
    public void onQueryTextChange_shouldNotTrackInMixpanel() throws Exception {
        adapter.onQueryTextChange("ne");
        verify(mixpanelAPI, never()).track(anyString(), any(JSONObject.class));
    }

    @Test
    public void onQueryTextChange_shouldTrackInMixpanel() throws Exception {
        String term = "new";
        adapter.onQueryTextChange(term);
        JSONObject expectedPayload = new JSONObject();
        expectedPayload.put(PELIAS_TERM, term);
        verify(mixpanelAPI).track(eq(PELIAS_SUGGEST), refEq(expectedPayload));
    }

    @Test
    public void onQueryTextChange_shouldSendLatLonOfMapPosition() throws Exception {
        MapPosition position = getMapController().getMapPosition();
        double expectedLat = 10.0;
        double expectedLon = 20.0;
        position.setPosition(expectedLat, expectedLon);
        getMapController().getMap().setMapPosition(position);
        adapter.onQueryTextChange("new query");
        verify(pelias).suggest(eq("new query"), eq(position.getLatitude()),
                eq(position.getLongitude()), any(Callback.class));
    }

    @Test
    public void onQueryTextSubmit_shouldBeFalse() {
        assertThat(adapter.onQueryTextSubmit(baseActivity.getString(R.string.secret_phrase)))
                .isFalse();
    }

    @Test
    public void onQueryTextSubmit_shouldToggleDebugMode() {
        Boolean debugMode = baseActivity.isInDebugMode();
        adapter.onQueryTextSubmit(baseActivity.getString(R.string.secret_phrase));
        assertThat(baseActivity.isInDebugMode()).isNotEqualTo(debugMode);
    }

    @Test
    public void loadSavedSearches_shouldChangeCursor() throws Exception {
        savedSearch.store("saved query 1");
        savedSearch.store("saved query 2");
        savedSearch.store("saved query 3");
        adapter.loadSavedSearches();
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        assertThat(cursor.getString(1)).isEqualTo("saved query 3");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 2");
        cursor.moveToNext();
        assertThat(cursor.getString(1)).isEqualTo("saved query 1");
    }

    @Test
    public void loadSavedSearches_shouldDisplayTerms() throws Exception {
        savedSearch.store("saved query 1");
        savedSearch.store("saved query 2");
        savedSearch.store("saved query 3");
        adapter.loadSavedSearches();
        TextView tv1 = new TextView(application);
        TextView tv2 = new TextView(application);
        TextView tv3 = new TextView(application);
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        adapter.bindView(tv1, application, cursor);
        cursor.moveToNext();
        adapter.bindView(tv2, application, cursor);
        cursor.moveToNext();
        adapter.bindView(tv3, application, cursor);
        assertThat(tv1).hasText("saved query 3");
        assertThat(tv2).hasText("saved query 2");
        assertThat(tv3).hasText("saved query 1");
    }

    @Test
    public void onClick_shouldExecuteSavedSearch() throws Exception {
        savedSearch.store("saved query");
        adapter.loadSavedSearches();
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        View view = adapter.newView(application, cursor, adapter.getSearchView());
        adapter.bindView(view, application, cursor);
        view.performClick();
        assertThat(adapter.getSearchView().getQuery().toString()).isEqualTo("saved query");
    }

    @Test
    public void bindView_shouldHighlightText() throws Exception {
        adapter.getSearchView().setQuery("New York", false);
        MatrixCursor cursor = (MatrixCursor) adapter.getCursor();
        cursor.moveToFirst();
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setProperty(TEXT, "New York, NY");
        byte[] data = ParcelableUtil.marshall(simpleFeature);
        cursor.addRow(new Object[]{0, data});
        TextView textView = new TextView(application);
        adapter.bindView(textView, application, cursor);
        Spanned spanned = (Spanned) textView.getText();
        ForegroundColorSpan[] foregroundColorSpans = spanned.getSpans(0, spanned.length(),
                ForegroundColorSpan.class);
        final int highlightColor = application.getResources().getColor(R.color.red);
        assertSpan(spanned, foregroundColorSpans[0], 0, 3, highlightColor);
        assertSpan(spanned, foregroundColorSpans[1], 4, 8, highlightColor);
    }

    @Test
    public void onQueryTextSubmit_shouldHideAutoCompleteListView() throws Exception {
        baseActivity.getAutoCompleteListView().setVisibility(View.VISIBLE);
        adapter.onQueryTextSubmit("query");
        assertThat(baseActivity.getAutoCompleteListView()).isGone();
    }

    @Test
    public void onClick_shouldHideAutoCompleteListView() throws Exception {
        baseActivity.getAutoCompleteListView().setVisibility(View.VISIBLE);
        view.performClick();
        assertThat(baseActivity.getAutoCompleteListView()).isGone();
    }

    @Test
    public void bindView_shouldDisplaySearchIconNextToSearchTerms() throws Exception {
        savedSearch.store("search term");
        adapter.loadSavedSearches();
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        TextView textView = new TextView(app);
        adapter.bindView(textView, app, cursor);

        Drawable expected = app.getResources().getDrawable(R.drawable.ic_recent);
        Drawable actual = textView.getCompoundDrawables()[0];
        assertDrawable(expected, actual);
    }

    @Test
    public void bindView_shouldSetPinIconForAutoCompleteResults() throws Exception {
        MatrixCursor cursor = (MatrixCursor) adapter.getCursor();
        cursor.moveToFirst();
        SimpleFeature simpleFeature = new SimpleFeature();
        simpleFeature.setProperty(TEXT, "New York, NY");
        byte[] data = ParcelableUtil.marshall(simpleFeature);
        cursor.addRow(new Object[]{0, data});
        TextView textView = new TextView(application);
        adapter.bindView(textView, application, cursor);

        Drawable expected = app.getResources().getDrawable(R.drawable.ic_pin_outline);
        Drawable actual = textView.getCompoundDrawables()[0];
        assertDrawable(expected, actual);
    }

    @Test
    public void success_shouldHideAutoCompleteListHeader() throws Exception {
        baseActivity.getAutoCompleteListView().showHeader();
        adapter.success(new Result(), null);
        assertThat(baseActivity.getAutoCompleteListView().isHeaderVisible()).isFalse();
    }

    @Test
    public void onQueryTextChange_shouldShowAutoCompleteListHeader() throws Exception {
        savedSearch.store("search term");
        baseActivity.getAutoCompleteListView().hideHeader();
        adapter.onQueryTextChange("");
        assertThat(baseActivity.getAutoCompleteListView().isHeaderVisible()).isTrue();
    }

    @Test
    public void onQueryTextChange_shouldHideListHeaderIfNoRecents() throws Exception {
        savedSearch.clear();
        baseActivity.getAutoCompleteListView().showHeader();
        adapter.onQueryTextChange("");
        assertThat(baseActivity.getAutoCompleteListView().isHeaderVisible()).isFalse();
    }

    @Test
    public void onQueryTextChange_shouldHideActionViewAll() throws Exception {
        baseActivity.showActionViewAll();
        adapter.onQueryTextChange("");
        assertThat(menu.findItem(R.id.action_view_all)).isNotVisible();
    }

    private void assertDrawable(Drawable expected, Drawable actual) {
        assertThat(shadowOf(actual).getCreatedFromResId())
                .isEqualTo(shadowOf(expected).getCreatedFromResId());
    }
}

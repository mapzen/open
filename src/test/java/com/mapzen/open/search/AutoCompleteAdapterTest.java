package com.mapzen.open.search;

import com.mapzen.android.gson.Result;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.adapters.SearchViewAdapter;
import com.mapzen.android.Pelias;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.fragment.ItemFragment;
import com.mapzen.open.support.DummyActivity;
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
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.database.TestCursor;
import org.robolectric.util.ActivityController;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.app.FragmentManager;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import javax.inject.Inject;

import retrofit.Callback;

import static com.mapzen.open.MapController.getMapController;
import static com.mapzen.open.entity.SimpleFeature.ID;
import static com.mapzen.open.entity.SimpleFeature.TEXT;
import static com.mapzen.open.entity.SimpleFeature.TYPE;
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
import static org.robolectric.Robolectric.buildActivity;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class AutoCompleteAdapterTest {
    private AutoCompleteAdapter adapter;
    private BaseActivity baseActivity;
    private TextView view;
    private FragmentManager fragmentManager;
    private SimpleFeature simpleFeature;
    @Inject Pelias pelias;
    @Inject MixpanelAPI mixpanelAPI;
    @Inject SavedSearch savedSearch;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback<Result>> callback;

    @Before
    public void setUp() throws Exception {
        ((MapzenApplication) application).inject(this);
        MockitoAnnotations.initMocks(this);
        savedSearch.clear();
        ActivityController<DummyActivity> controller = buildActivity(DummyActivity.class);
        controller.create().start().resume();
        fragmentManager = controller.get().getSupportFragmentManager();
        baseActivity = TestHelper.initBaseActivity();
        adapter = new AutoCompleteAdapter(baseActivity.getActionBar().getThemedContext(),
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
        SearchViewAdapter searchViewAdapter = (SearchViewAdapter)
                pagerResultsFragment.pager.getAdapter();
        ItemFragment itemFragment = (ItemFragment) searchViewAdapter.getItem(0);
        assertThat(itemFragment.getSimpleFeature()).isSameAs(simpleFeature);
    }

    @Test
    public void onQueryTextChange_shouldAttachAdapterIfNull() throws Exception {
        baseActivity.executeSearchOnMap("query");
        adapter.onQueryTextChange("new query");
        assertThat(baseActivity.getSearchView().getSuggestionsAdapter()).isNotNull();
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
        verify(pelias).suggest(eq("new query"), eq(String.valueOf(position.getLatitude())),
                eq(String.valueOf(position.getLongitude())), any(Callback.class));
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
    public void onClick_shouldSaveCurrentAutoCompleteQuery() throws Exception {
        view.performClick();
        assertThat(((MapzenApplication) application).getCurrentSearchTerm())
                .isEqualTo(simpleFeature.getHint());
    }

    @Test
    public void onQueryTextSubmit_shouldSetSelection() throws Exception {
        SearchView searchView = adapter.getSearchView();
        EditText editText = (EditText) searchView.findViewById(application.getResources()
                .getIdentifier("android:id/search_src_text", null, null));

        searchView.setQuery("query", false);
        editText.setSelection(1);
        adapter.onQueryTextSubmit("query");
        assertThat(editText).hasSelectionStart(0);
    }

    @Test
    public void onClick_shouldSetSelection() throws Exception {
        SearchView searchView = adapter.getSearchView();
        EditText editText = (EditText) searchView.findViewById(application.getResources()
                .getIdentifier("android:id/search_src_text", null, null));

        view.setText("query");
        view.performClick();
        assertThat(editText).hasSelectionStart(0);
    }
}

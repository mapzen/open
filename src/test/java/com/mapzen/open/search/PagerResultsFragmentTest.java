package com.mapzen.open.search;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.android.Pelias;
import com.mapzen.android.gson.Feature;
import com.mapzen.android.gson.Result;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscim.core.MapPosition;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowNetworkInfo;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.util.FragmentTestUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.mapzen.open.MapController.getMapController;
import static com.mapzen.open.search.SavedSearch.getSavedSearch;
import static com.mapzen.open.support.TestHelper.getTestFeature;
import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.open.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.open.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PagerResultsFragmentTest {
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback<Result>> peliasCallback;
    private PagerResultsFragment fragment;
    private MapzenApplication app;
    private BaseActivity act;
    private TestMenu menu;
    @Inject Pelias pelias;

    @Before
    public void setUp() throws Exception {
        app = (MapzenApplication) application;
        app.inject(this);
        MockitoAnnotations.initMocks(this);
        menu = new TestMenu();
        act = initBaseActivityWithMenu(menu);
        initMapFragment(act);
        fragment = PagerResultsFragment.newInstance(act);
        FragmentTestUtil.startFragment(fragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldInjectViewPager() throws Exception {
        assertThat(fragment.pager).isNotNull();
    }

    @Test
    public void shouldInjectPaginationIndicator() throws Exception {
        assertThat(fragment.indicator).isNotNull();
    }

    @Test
    public void displayResults_shouldSetPaginationIndicatorText() throws Exception {
        fragment.add(getTestSimpleFeature());
        fragment.add(getTestSimpleFeature());
        fragment.add(getTestSimpleFeature());
        fragment.displayResults(3, 0);
        assertThat(fragment.indicator).hasText("Viewing 1 of 3 results");
    }

    @Test
    public void shouldInjectViewAllButton() throws Exception {
        assertThat(fragment.viewAll).isNotNull();
        assertThat(fragment.viewAll).hasText(act.getString(R.string.view_all));
    }

    @Test
    public void executeSearchOnMap_shouldDismissProgressDialogOnError() throws Exception {
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        verify(pelias).search(eq("Empire State Building"), anyString(), anyString(),
                peliasCallback.capture());
        peliasCallback.getValue().failure(RetrofitError.unexpectedError("", null));
        assertThat(act.getMapFragment().getView().findViewById(R.id.map)).isVisible();
        assertThat(act.getMapFragment().getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void executeSearchOnMap_shouldToastAnError() {
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        verify(pelias).search(eq("Empire State Building"), anyString(), anyString(),
                peliasCallback.capture());

        peliasCallback.getValue().failure(RetrofitError.unexpectedError("", null));
        assertThat(getTextOfLatestToast()).isEqualTo(app.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }

    @Test
    public void executeSearchOnMap_shouldSaveSearchTerm() {
        fragment.executeSearchOnMap(new SearchView(app), "Some fantastic term");
        assertThat(getSavedSearch().get().next()).isEqualTo("Some fantastic term");
    }

    @Test
    public void executeSearchOnMap_shouldSendLatLonOfMapPosition() {
        MapPosition position = getMapController().getMapPosition();
        double expectedLat = 10.0;
        double expectedLon = 20.0;
        position.setPosition(expectedLat, expectedLon);
        getMapController().getMap().setMapPosition(position);
        fragment.executeSearchOnMap(new SearchView(app), "Some fantastic term");
        verify(pelias).search(eq("Some fantastic term"), eq(String.valueOf(position.getLatitude())),
                eq(String.valueOf(position.getLongitude())), any(Callback.class));
    }

    @Test
    public void viewAll_shouldStartListResultsActivity() throws Exception {
        fragment.viewAll.performClick();
        assertThat(getShadowApplication().getNextStartedActivity())
                .hasComponent(application.getPackageName(), ListResultsActivity.class);
    }

    @Test
    public void viewAll_shouldParcelFeatureList() throws Exception {
        fragment.viewAll.performClick();
        assertThat(getShadowApplication().getNextStartedActivity())
                .hasExtra(ListResultsActivity.EXTRA_FEATURE_LIST);
    }

    @Test
    public void viewAll_shouldParcelSearchTermForCurrentResults() throws Exception {
        fragment.executeSearchOnMap(new SearchView(app), "Some fantastic term");
        ImageView closeButton = (ImageView) act.getSearchView().findViewById(act.getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        closeButton.performClick();
        fragment.viewAll.performClick();
        assertThat(getShadowApplication().getNextStartedActivity()
                .getStringExtra(ListResultsActivity.EXTRA_SEARCH_TERM))
                .isEqualTo("Some fantastic term");
    }

    @Test
    public void displayResults_shouldShowMultiResultHeaderForMultipleResults() throws Exception {
        fragment.add(new SimpleFeature());
        fragment.add(new SimpleFeature());
        fragment.displayResults(2, 0);
        assertThat(fragment.multiResultHeader).isVisible();
    }

    @Test
    public void displayResults_shouldHideMultiResultHeaderForSingleResult() throws Exception {
        fragment.add(new SimpleFeature());
        fragment.displayResults(1, 0);
        assertThat(fragment.multiResultHeader).isGone();
    }

    @Test
    public void onFocusChange_shouldShowMostRecentQuery() throws Exception {
        app.setCurrentSearchTerm("current query");
        fragment.onAttach(act);
        SearchView searchView = act.getSearchView();
        AutoCompleteTextView autoCompleteTextView = act.getQueryAutoCompleteTextView(searchView);
        autoCompleteTextView.getOnFocusChangeListener().onFocusChange(autoCompleteTextView, true);
        assertThat(act.getSearchView().getQuery().toString()).isEqualTo("current query");
    }

    @Test
    public void onClickCloseButton_shouldClearQuery() throws Exception {
        app.setCurrentSearchTerm("current query");
        SearchView searchView = act.getSearchView();
        searchView.setQuery("current query", false);
        ImageView closeButton = (ImageView) act.getSearchView().findViewById(act.getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        closeButton.performClick();
        assertThat(searchView.getQuery().toString()).isEmpty();
        assertThat(app.getCurrentSearchTerm()).isEmpty();
    }

    @Test
    public void onClickCloseButton_shouldFocusQueryTextView() throws Exception {
        SearchView searchView = act.getSearchView();
        AutoCompleteTextView autoCompleteTextView = act.getQueryAutoCompleteTextView(searchView);
        ImageView closeButton = (ImageView) act.getSearchView().findViewById(act.getResources()
                .getIdentifier("android:id/search_close_btn", null, null));

        closeButton.performClick();
        assertThat(autoCompleteTextView).hasFocus();
    }

    @Test
    public void onClickCloseButton_shouldLoadSavedSearches() throws Exception {
        getSavedSearch().clear();
        getSavedSearch().store("saved query 1");
        getSavedSearch().store("saved query 2");
        getSavedSearch().store("saved query 3");
        ImageView closeButton = (ImageView) act.getSearchView().findViewById(act.getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        closeButton.performClick();
        assertThat(act.getSearchView().getSuggestionsAdapter()).hasCount(3);
    }

    @Test
    public void onActivityResult_shouldSetPagerIndex() throws Exception {
        final int expected = 2;
        Intent intent = new Intent();
        intent.putExtra(ListResultsActivity.EXTRA_INDEX, expected);
        fragment.add(getTestSimpleFeature());
        fragment.add(getTestSimpleFeature());
        fragment.add(getTestSimpleFeature());
        fragment.displayResults(3, 0);
        fragment.onActivityResult(0, Activity.RESULT_OK, intent);
        assertThat(fragment.pager).hasCurrentItem(expected);
    }

    @Test
    public void onResume_shouldPostRunnableToClearSearchViewFocus() throws Exception {
        act.getSearchView().requestFocus();
        fragment.onResume();
        assertThat(act.getSearchView()).isNotFocused();
    }

    @Test
    public void shouldHideRootLayout() throws Exception {
        assertThat(fragment.getView().findViewById(R.id.results_root)).isNotVisible();
    }

    @Test
    public void displayResults_shouldRevealRootLayout() throws Exception {
        fragment.add(getTestSimpleFeature());
        fragment.displayResults(1, 0);
        assertThat(fragment.getView().findViewById(R.id.results_root)).isVisible();
    }

    @Test
    public void executeSearchOnMap_shouldToastErrorIfNetworkNotAvailable() throws Exception {
        simulateNoNetworkConnection();
        fragment.executeSearchOnMap(act.getSearchView(), "query");
        assertThat(getTextOfLatestToast()).isEqualTo(act.getString(R.string.no_network));
    }

    @Test
    public void executeSearchOnMap_shouldReturnFalseIfNetworkNotAvailable() throws Exception {
        simulateNoNetworkConnection();
        assertThat(fragment.executeSearchOnMap(act.getSearchView(), "query")).isFalse();
    }

    @Test
    public void setSearchResults_shouldCheckForNullPager() throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(getTestFeature());
        fragment.pager = null;
        fragment.setSearchResults(features);
        List<ShadowLog.LogItem> logs = ShadowLog.getLogs();
        assertThat(logs.get(logs.size() - 1).msg)
                .isEqualTo("Unable to display search results: pager is null");
    }

    private void simulateNoNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) act.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        ShadowNetworkInfo shadowNetworkInfo = shadowOf(networkInfo);
        shadowNetworkInfo.setConnectionStatus(false);
    }
}

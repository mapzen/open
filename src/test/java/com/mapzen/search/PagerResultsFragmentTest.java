package com.mapzen.search;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.PeliasService;
import com.mapzen.android.TestPelias;
import com.mapzen.android.gson.Result;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.util.MapzenProgressDialogFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.util.FragmentTestUtil;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;

import static com.mapzen.search.SavedSearch.getSavedSearch;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.getShadowApplication;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PagerResultsFragmentTest {
    PeliasService peliasServiceMock;
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<Callback<Result>> peliasCallback;
    private PagerResultsFragment fragment;
    private MapzenApplication app;
    private BaseActivity act;
    private TestMenu menu;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        peliasServiceMock = TestPelias.getPeliasMock();
        menu = new TestMenu();
        act = initBaseActivityWithMenu(menu);
        initMapFragment(act);
        fragment = PagerResultsFragment.newInstance(act);
        FragmentTestUtil.startFragment(fragment);
        app = (MapzenApplication) application;
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
        assertThat(fragment.viewAll).hasText("View All");
    }

    @Test
    public void executeSearchOnMap_shouldDismissProgressDialogOnError() throws Exception {
        MapzenProgressDialogFragment dialogFragment = act.getProgressDialogFragment();
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        assertThat(dialogFragment).isAdded();
        verify(peliasServiceMock).getSearch(eq("Empire State Building"), anyString(),
                peliasCallback.capture());
        peliasCallback.getValue().failure(RetrofitError.unexpectedError("", null));
        assertThat(dialogFragment).isNotAdded();
    }

    @Test
    public void executeSearchOnMap_shouldToastAnError() {
        fragment.executeSearchOnMap(new SearchView(app), "Empire State Building");
        assertThat(act.getProgressDialogFragment()).isAdded();
        verify(peliasServiceMock).getSearch(eq("Empire State Building"), anyString(),
                peliasCallback.capture());
        peliasCallback.getValue().failure(RetrofitError.unexpectedError("", null));
        assertThat(ShadowToast.getTextOfLatestToast())
                .isEqualTo(app.getString(R.string.generic_server_error));
        assertThat(ShadowToast.getLatestToast()).hasDuration(Toast.LENGTH_LONG);
    }

    @Test
    public void executeSearchOnMap_shouldSaveSearchTerm() {
        fragment.executeSearchOnMap(new SearchView(app), "Some fantastic term");
        assertThat(getSavedSearch().get().next()).isEqualTo("Some fantastic term");
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
    public void onAttach_shouldHideOverflowMenu() throws Exception {
        Menu menu = new TestMenu();
        act.onCreateOptionsMenu(menu);
        fragment.onAttach(act);
        assertThat(menu.findItem(R.id.settings)).isNotVisible();
        assertThat(menu.findItem(R.id.phone_home)).isNotVisible();
        assertThat(menu.findItem(R.id.login)).isNotVisible();
    }

    @Test
    public void onDetach_shouldShowOverflowMenu() throws Exception {
        Menu menu = new TestMenu();
        act.onCreateOptionsMenu(menu);
        act.hideOverflowMenu();
        fragment.onDetach();
        assertThat(menu.findItem(R.id.settings)).isVisible();
        assertThat(menu.findItem(R.id.phone_home)).isVisible();
        assertThat(menu.findItem(R.id.login)).isVisible();
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
}


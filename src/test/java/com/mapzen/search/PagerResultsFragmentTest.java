package com.mapzen.search;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.android.PeliasService;
import com.mapzen.android.TestPelias;
import com.mapzen.android.gson.Result;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.ListResultsFragment;
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

import android.widget.SearchView;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RetrofitError;

import static com.mapzen.search.SavedSearch.getSavedSearch;
import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Robolectric.application;

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
    public void shouldInjectViewAllButton() throws Exception {
        assertThat(fragment.viewAll).isNotNull();
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
    public void viewAll_shouldAddListResultsFragment() throws Exception {
        fragment.viewAll.performClick();
        assertThat(act.getSupportFragmentManager()).hasFragmentWithTag(ListResultsFragment.TAG);
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
}


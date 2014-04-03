package com.mapzen.search;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.SearchViewAdapter;
import com.mapzen.entity.GeoFeature;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.support.DummyActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.database.TestCursor;
import org.robolectric.util.ActivityController;

import static com.mapzen.support.TestHelper.getTestGeoFeature;
import static com.mapzen.support.TestHelper.initMapFragment;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class AutoCompleteAdapterTest {
    private AutoCompleteAdapter adapter;
    private BaseActivity baseActivity;
    private View view;
    private FragmentManager fragmentManager;
    private GeoFeature geoFeature;

    @Before
    public void setUp() throws Exception {
        ActivityController<DummyActivity> controller = buildActivity(DummyActivity.class);
        controller.create().start().resume();
        fragmentManager = controller.get().getSupportFragmentManager();
        baseActivity = TestHelper.initBaseActivity();
        adapter = new AutoCompleteAdapter(baseActivity.getActionBar().getThemedContext(),
                baseActivity, ((MapzenApplication) application).getColumns(), fragmentManager);
        adapter.setSearchView(baseActivity.getSearchView());
        adapter.setMapFragment(initMapFragment(baseActivity));
        view = adapter.newView(baseActivity, new TestCursor(), new FrameLayout(baseActivity));
        geoFeature = getTestGeoFeature();
        view.setTag(geoFeature);
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
        assertThat(itemFragment.getGeoFeature()).isSameAs(geoFeature);
    }

    @Test
    public void onQueryTextChange_shouldAttachAdapterIfNull() throws Exception {
        baseActivity.executeSearchOnMap("query");
        adapter.onQueryTextChange("new query");
        assertThat(baseActivity.getSearchView().getSuggestionsAdapter()).isNotNull();
    }
}

package com.mapzen.search;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.adapters.SearchViewAdapter;
import com.mapzen.entity.SimpleFeature;
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

import android.database.Cursor;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import static com.mapzen.search.SavedSearch.getSavedSearch;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
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
    private TextView view;
    private FragmentManager fragmentManager;
    private SimpleFeature simpleFeature;

    @Before
    public void setUp() throws Exception {
        getSavedSearch().clear();
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
        assertThat(getSavedSearch().get().next()).isEqualTo("saved term");
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
    public void loadSavedSearches_shouldChangeCursor() throws Exception {
        getSavedSearch().store("saved query 1");
        getSavedSearch().store("saved query 2");
        getSavedSearch().store("saved query 3");
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
        getSavedSearch().store("saved query 1");
        getSavedSearch().store("saved query 2");
        getSavedSearch().store("saved query 3");
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
        getSavedSearch().store("saved query");
        adapter.loadSavedSearches();
        Cursor cursor = adapter.getCursor();
        cursor.moveToFirst();
        View view = adapter.newView(application, cursor, adapter.getSearchView());
        adapter.bindView(view, application, cursor);
        view.performClick();
        assertThat(adapter.getSearchView().getQuery().toString()).isEqualTo("saved query");
    }

    @Test
    public void onClick_shouldSaveCurrentAutoCompleteQuery() throws Exception {
        view.performClick();
        assertThat(((MapzenApplication) application).getCurrentSearchTerm())
                .isEqualTo(simpleFeature.getHint());
    }
}
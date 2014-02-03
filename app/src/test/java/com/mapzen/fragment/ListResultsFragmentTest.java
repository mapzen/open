package com.mapzen.fragment;

import android.view.MenuItem;

import com.mapzen.MapzenTestRunner;
import com.mapzen.entity.Feature;
import com.mapzen.support.TestBaseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.tester.android.view.TestMenuItem;

import java.util.ArrayList;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(MapzenTestRunner.class)
public class ListResultsFragmentTest {

    private TestBaseActivity act;
    private ListResultsFragment fragment;

    @Before
    public void setUp() throws Exception {
        act = Robolectric.buildActivity(TestBaseActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        fragment = ListResultsFragment.newInstance(act, null);
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldHaveListAdapter() throws Exception {
        Feature feature = new Feature();
        ArrayList<Feature> list = new ArrayList<Feature>();
        list.add(feature);
        fragment = ListResultsFragment.newInstance(act, list);
        assertThat(fragment.getListAdapter()).hasCount(1);
    }

    @Test
    public void onOptionsItemHomeSelected_shouldPopBackStack() throws Exception {
        Feature feature = new Feature();
        ArrayList<Feature> list = new ArrayList<Feature>();
        list.add(feature);
        fragment = ListResultsFragment.newInstance(act, list);
        startFragment(fragment);
        MenuItem menu = new TestMenuItem(android.R.id.home);
        fragment.onOptionsItemSelected(menu);
        assertThat(act.getBackPressed()).isTrue();
    }
}

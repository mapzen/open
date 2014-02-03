package com.mapzen.fragment;

import android.view.MenuItem;

import com.mapzen.MapzenTestRunner;
import com.mapzen.entity.Feature;
import com.mapzen.support.TestActionBar;
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
    private ListResultsFragment fragment;
    private TestBaseActivity act;

    @Before
    public void setUp() throws Exception {
        act = Robolectric.buildActivity(TestBaseActivity.class).create().get();
        Feature feature = new Feature();
        ArrayList<Feature> list = new ArrayList<Feature>();
        list.add(feature);
        fragment = ListResultsFragment.newInstance(act, list);
        startFragment(fragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldHaveListAdapter() throws Exception {
        assertThat(fragment.getListAdapter()).hasCount(1);
    }

    @Test
    public void onViewCreated_shouldEnableHomeAsUp() throws Exception {
        assertThat(((TestActionBar) act.getActionBar()).getHomeButtonEnabled()).isTrue();
        assertThat(((TestActionBar) act.getActionBar()).getDisplayHomeAsUpEnabled()).isTrue();
    }

    @Test
    public void onViewCreated_shouldSetActionBarTitle() throws Exception {
        assertThat(act.getActionBar().getTitle()).isEqualTo("Results");
    }

    @Test
    public void onOptionsItemSelected_shouldPopBackStack() throws Exception {
        MenuItem menu = new TestMenuItem(android.R.id.home);
        fragment.onOptionsItemSelected(menu);
        assertThat(act.getBackPressed()).isTrue();
    }

    @Test
    public void onDetach_shouldDisableHomeAsUp() throws Exception {
        fragment.onDetach();
        assertThat(((TestActionBar) act.getActionBar()).getHomeButtonEnabled()).isFalse();
        assertThat(((TestActionBar) act.getActionBar()).getDisplayHomeAsUpEnabled()).isFalse();
    }

    @Test
    public void onDetach_shouldResetActionBarTitle() throws Exception {
        fragment.onDetach();
        assertThat(act.getActionBar().getTitle()).isEqualTo("Mapzen");
    }
}

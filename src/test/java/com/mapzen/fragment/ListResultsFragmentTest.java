package com.mapzen.fragment;

import com.mapzen.entity.SimpleFeature;
import com.mapzen.search.ListResultsActivity;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ListResultsFragmentTest {
    private ListResultsFragment fragment;

    @Before
    public void setUp() throws Exception {
        ArrayList<SimpleFeature> list = new ArrayList<SimpleFeature>();
        list.add(getTestSimpleFeature());
        list.add(getTestSimpleFeature());
        list.add(getTestSimpleFeature());
        fragment = ListResultsFragment.newInstance(list);
        startFragment(fragment);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldHaveListAdapter() throws Exception {
        assertThat(fragment.getListAdapter()).hasCount(3);
    }

    @Test
    public void onListItemClick_shouldSetResult() throws Exception {
        final int expected = 2;
        fragment.onListItemClick(fragment.getListView(), null, expected, 0);
        assertThat(Robolectric.shadowOf(fragment.getActivity()).getResultIntent()
                .getIntExtra(ListResultsActivity.EXTRA_INDEX, 0)).isEqualTo(expected);
    }

    @Test
    public void onListItemClick_shouldFinishActivity() throws Exception {
        fragment.onListItemClick(fragment.getListView(), null, 0, 0);
        assertThat(fragment.getActivity()).isFinishing();
    }
}

package com.mapzen.fragment;

import com.mapzen.MapzenTestRunner;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;
import com.mapzen.entity.Feature;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.fest.assertions.api.ANDROID.assertThat;

@RunWith(MapzenTestRunner.class)
public class ListResultsFragmentTest {

    private BaseActivity act;
    private ListResultsFragment fragment;

    @Before
    public void setUp() throws Exception {
        act = Robolectric.buildActivity(BaseActivity.class).create().get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        fragment = ListResultsFragment.newInstance(act, null);
        assertThat(fragment).isNotNull();
    }

    @Test
    public void shouldNotBeAttached() throws Exception {
        fragment = ListResultsFragment.newInstance(act, null);
        assert (!fragment.isAdded());
    }

    @Test
    public void shouldBeAttached() throws Exception {
        fragment = ListResultsFragment.newInstance(act, null);
        fragment.attachToContainer(R.id.full_list);
        assert (fragment.isAdded());
    }

    @Test
    public void shouldBeHidden() throws Exception {
        fragment = ListResultsFragment.newInstance(act, null);
        fragment.attachToContainer(R.id.full_list);
        fragment.detach();
        assert (fragment.isHidden());
    }

    @Test
    public void shouldHaveListAdapter() throws Exception {
        Feature feature = new Feature();
        ArrayList<Feature> list = new ArrayList<Feature>();
        list.add(feature);
        fragment = ListResultsFragment.newInstance(act, list);
        assert (fragment.getListAdapter().getCount() == 1);
    }
}

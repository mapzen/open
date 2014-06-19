package com.mapzen.fragment;

import com.mapzen.entity.SimpleFeature;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenu;

import java.util.ArrayList;

import static com.mapzen.support.TestHelper.initBaseActivityWithMenu;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ListResultsFragmentTest {
    private ListResultsFragment fragment;
    private TestBaseActivity act;
    private TestMenu menu;

    @Before
    public void setUp() throws Exception {
        menu = new TestMenu();
        act = initBaseActivityWithMenu(menu);
        SimpleFeature simpleFeature = new SimpleFeature();
        ArrayList<SimpleFeature> list = new ArrayList<SimpleFeature>();
        list.add(simpleFeature);
        fragment = ListResultsFragment.newInstance(list);
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
}

package com.mapzen.search;

import com.mapzen.R;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

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
        fragment = ListResultsFragment.newInstance(list, "term");
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
    public void shouldDisplayCurrentSearchTerm() throws Exception {
        Spanned expected = Html.fromHtml("&ldquo;term&rdquo;");
        assertThat((TextView) fragment.getListView().findViewById(R.id.term)).hasText(expected);
    }

    @Test
    public void onListItemClick_shouldSetResult() throws Exception {
        final int expected = 2;
        final int index = 3;
        fragment.onListItemClick(fragment.getListView(), null, index, 0);
        assertThat(Robolectric.shadowOf(fragment.getActivity()).getResultIntent()
                .getIntExtra(ListResultsActivity.EXTRA_INDEX, 0)).isEqualTo(expected);
    }

    @Test
    public void onListItemClick_shouldFinishActivity() throws Exception {
        fragment.onListItemClick(fragment.getListView(), null, 0, 0);
        assertThat(fragment.getActivity()).isFinishing();
    }
}

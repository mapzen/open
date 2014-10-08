package com.mapzen.open.search;

import com.mapzen.open.R;
import com.mapzen.open.entity.SimpleFeature;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenuItem;

import android.app.ActionBar;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.open.support.TestHelper.getTestSimpleFeature;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.getShadowApplication;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class ListResultsActivityTest {
    private ListResultsActivity activity;
    private ListResultsFragment fragment;

    @Before
    public void setUp() throws Exception {
        ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
        features.add(getTestSimpleFeature());
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(ListResultsActivity.EXTRA_FEATURE_LIST, features);
        intent.putExtra(ListResultsActivity.EXTRA_SEARCH_TERM, "term");
        activity = buildActivity(ListResultsActivity.class)
                .withIntent(intent)
                .create()
                .start()
                .resume()
                .visible()
                .get();
        fragment = (ListResultsFragment) activity.getSupportFragmentManager().getFragments().get(0);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldHaveTitle() throws Exception {
        assertThat(getShadowApplication().getAppManifest()
                .getActivityLabel(ListResultsActivity.class)).isEqualTo("@string/results_title");
    }

    @Test
    public void shouldSetActionBarDisplayOptions() throws Exception {
        assertThat(activity.getActionBar()).hasDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
                | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_USE_LOGO);
    }

    @Test
    public void shouldFinishActivityOnOptionsItemHomeSelected() throws Exception {
        activity.onOptionsItemSelected(new TestMenuItem(android.R.id.home));
        assertThat(activity).isFinishing();
    }

    @Test
    public void shouldDisplayFeaturesFromIntentExtra() throws Exception {
        assertThat(fragment.getListAdapter().getItem(0))
                .isEqualsToByComparingFields(getTestSimpleFeature());
    }

    @Test
    public void shouldDisplaySearchTermFromIntentExtra() throws Exception {
        Spanned expected = Html.fromHtml("&ldquo;term&rdquo;");
        assertThat((TextView) fragment.getListView().findViewById(R.id.term)).hasText(expected);
    }
}

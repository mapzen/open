package com.mapzen.route;

import com.mapzen.MapController;
import com.mapzen.R;
import com.mapzen.TestMapzenApplication;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.support.TestHelper.getFixture;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class RoutePreviewFragmentTest {
    TestBaseActivity activity;
    RoutePreviewFragment fragment;
    SimpleFeature destination;
    @Inject Router router;
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<double[]> location;

    @Before
    public void setup() throws Exception {
        ((TestMapzenApplication) Robolectric.application).inject(this);
        MockitoAnnotations.initMocks(this);
        activity = initBaseActivity();
        destination = getTestSimpleFeature();
        fragment = RoutePreviewFragment.newInstance(activity, destination);
        FragmentTestUtil.startFragment(fragment);
    }

    @Test
    public void shouldHaveTag() throws Exception {
        assertThat(RoutePreviewFragment.TAG).isEqualTo(RoutePreviewFragment.class.getSimpleName());
    }

    @Test
    public void newInstance_shouldReturnInstanceOfRoutePreviewFragment() throws Exception {
        assertThat(RoutePreviewFragment.newInstance(activity, destination)).
                isInstanceOf(RoutePreviewFragment.class);
    }

    @Test
    public void setRouteTo_shouldClearMap() throws Exception {
        fragment.getMapFragment().addPoi(getTestSimpleFeature());
        fragment.createRouteToDestination();
        assertThat(fragment.getMapFragment().getPoiLayer().size()).isEqualTo(0);
    }

    @Test
    public void setRouteTo_shouldShowLoadingDialog() throws Exception {
        fragment.createRouteToDestination();
        assertThat(activity.getProgressDialogFragment()).isAdded();
    }

    @Test
    public void setRouteTo_successShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(activity.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void setRouteTo_failureShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteToDestination();
        fragment.failure(500);
        assertThat(activity.getProgressDialogFragment()).isNotAdded();
    }

    @Test
    public void onStart_shouldHideActionbar() throws Exception {
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onDetach_shouldShowActionbar() throws Exception {
        fragment.onDetach();
        assertThat(activity.getActionBar()).isShowing();
    }

    @Test
    public void onStart_shouldHaveCurrentLocation() throws Exception {
        TextView textView = (TextView) fragment.getView().findViewById(R.id.starting_point);
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(textView).isNotNull();
        assertThat(textView).hasText("Current Location");
    }

    @Test
    public void onStart_shouldHaveDestinationLocation() throws Exception {
        SimpleFeature feature = getTestSimpleFeature();
        TextView textView = (TextView) fragment.getView().findViewById(R.id.destination);
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(textView).isNotNull();
        assertThat(textView).hasText(feature.getProperty(NAME));
    }

    @Test
    public void reverse_shouldSwapOriginalLocationAndDestination() throws Exception {
        TextView startingPoint = (TextView) fragment.getView().findViewById(R.id.starting_point);
        TextView destination = (TextView) fragment.getView().findViewById(R.id.destination);
        SimpleFeature feature = getTestSimpleFeature();
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        fragment.reverse();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(destination).hasText("Current Location");
        assertThat(startingPoint).hasText(feature.getProperty(NAME));
    }

    @Test
    public void createRouteToDestination_shouldGetCurrentLocationFirst() throws Exception {
        MapController.getMapController().setLocation(getTestLocation(22.22, 44.44));
        fragment.createRouteToDestination();
        Mockito.verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] {22.22, 44.44});
        assertThat(values.get(1)).isEqualTo(new double[] {1.0, 1.0});
    }

    @Test
    public void createRouteToDestination_shouldGetFeatureDestinationFirst() throws Exception {
        MapController.getMapController().setLocation(getTestLocation(22.22, 44.44));
        fragment.reverse();
        Mockito.verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] {1.0, 1.0});
        assertThat(values.get(1)).isEqualTo(new double[] {22.22, 44.44});
    }
}

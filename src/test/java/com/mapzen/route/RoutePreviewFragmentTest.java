package com.mapzen.route;

import com.mapzen.R;
import com.mapzen.TestMapzenApplication;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.helpers.DistanceFormatter;
import com.mapzen.osrm.Route;
import com.mapzen.osrm.Router;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.widget.DistanceView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import android.location.Location;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToGeoPoint;
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
    @Inject PathLayer path;
    @Inject ItemizedLayer<MarkerItem> markers;
    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<double[]> location;

    @Captor
    @SuppressWarnings("unused")
    ArgumentCaptor<MarkerItem> marker;

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
        fragment.getView().findViewById(R.id.route_reverse).performClick();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(destination).hasText("Current Location");
        assertThat(startingPoint).hasText(feature.getProperty(NAME));
    }

    @Test
    public void createRouteToDestination_shouldGetCurrentLocationFirst() throws Exception {
        getMapController().setLocation(getTestLocation(22.22, 44.44));
        fragment.createRouteToDestination();
        Mockito.verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] {22.22, 44.44});
        assertThat(values.get(1)).isEqualTo(new double[] {1.0, 1.0});
    }

    @Test
    public void createRouteToDestination_shouldGetFeatureDestinationFirst() throws Exception {
        getMapController().setLocation(getTestLocation(22.22, 44.44));
        fragment.reverse();
        Mockito.verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] {1.0, 1.0});
        assertThat(values.get(1)).isEqualTo(new double[] {22.22, 44.44});
    }

    @Test
    public void success_shouldAddPathToMap() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(path)).isTrue();
    }

    @Test
    public void success_shouldNotAddPathToMapMoreThanOnce() throws Exception {
        int layersSize = getMapController().getMap().layers().size();
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(path)).isTrue();
    }

    @Test
    public void success_shouldClearPreviousRoutes() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        Mockito.verify(path).clearPath();
    }

    @Test
    public void failure_shouldClearPreviousRoutes() throws Exception {
        fragment.createRouteToDestination();
        fragment.failure(500);
        Mockito.verify(path).clearPath();
    }

    @Test
    public void success_shouldDrawRoute() throws Exception {
        fragment.createRouteToDestination();
        Route route = new Route(getFixture("around_the_block"));
        fragment.success(route);
        for (Location loc : route.getGeometry()) {
            Mockito.verify(path).addPoint(locationToGeoPoint(loc));
        }
    }

    @Test
    public void routeForCar_shouldRouteByCar() throws Exception {
        fragment.getView().findViewById(R.id.by_car).performClick();
        Mockito.verify(router).setDriving();
    }

    @Test
    public void routeForFoot_shouldRouteByFoot() throws Exception {
        fragment.getView().findViewById(R.id.by_foot).performClick();
        Mockito.verify(router).setWalking();
    }

    @Test
    public void routeForBike_shouldRouteByBike() throws Exception {
        fragment.getView().findViewById(R.id.by_bike).performClick();
        Mockito.verify(router).setBiking();
    }

    @Test
    public void success_shouldAddMarkerLayer() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(markers)).isTrue();
    }

    @Test
    public void success_shouldAddMarkerLayerOnce() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(markers)).isTrue();
    }

    @Test
    public void success_shouldClearMarkerLayer() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        Mockito.verify(markers).removeAllItems();
    }

    @Test
    public void success_shouldAddBubblesAandB() throws Exception {
        Route testRoute = new Route(getFixture("around_the_block"));
        ArrayList<Location> geometry = testRoute.getGeometry();
        fragment.createRouteToDestination();
        fragment.success(testRoute);
        Mockito.verify(markers, Mockito.times(2)).addItem(marker.capture());
        List<MarkerItem> values = marker.getAllValues();
        assertThat(values.get(0).getPoint().getLatitude()).
                isEqualTo(geometry.get(0).getLatitude());
        assertThat(values.get(0).getPoint().getLongitude()).
                isEqualTo(geometry.get(0).getLongitude());
        assertThat(values.get(1).getPoint().getLatitude()).
                isEqualTo(geometry.get(geometry.size() - 1).getLatitude());
        assertThat(values.get(1).getPoint().getLongitude()).
                isEqualTo(geometry.get(geometry.size() - 1).getLongitude());
    }

    @Test
    public void success_shouldPopulateDestinationPreview() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        TextView distanceToDestination =
                (TextView) fragment.getView().findViewById(R.id.destination_preview);
        assertThat(distanceToDestination).
                containsText(destination.getProperty(NAME));
    }

    @Test
    public void success_shouldPopulateDestinationPreviewDistance() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        TextView distanceToDestination =
                (DistanceView) fragment.getView().findViewById(R.id.destination_preview_distance);
        assertThat(distanceToDestination).
                containsText(DistanceFormatter.format(testRoute.getTotalDistance()));
    }

    @Test
    public void reverse_shouldPopulateDestinationPreviewWithCurrentLocation() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.reverse();
        TextView distanceToDestination =
                (TextView) fragment.getView().findViewById(R.id.destination_preview);
        assertThat(distanceToDestination).
                containsText("Current Location");
    }
}

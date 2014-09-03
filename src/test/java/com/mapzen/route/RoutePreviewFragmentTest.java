package com.mapzen.route;

import com.mapzen.MapzenApplication;
import com.mapzen.R;
import com.mapzen.TestMapzenApplication;
import com.mapzen.entity.SimpleFeature;
import com.mapzen.fragment.MapFragment;
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
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.FragmentTestUtil;

import android.content.Intent;
import android.location.Location;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.mapzen.MapController.getMapController;
import static com.mapzen.MapController.locationToGeoPoint;
import static com.mapzen.activity.BaseActivity.COM_MAPZEN_UPDATE_VIEW;
import static com.mapzen.entity.SimpleFeature.NAME;
import static com.mapzen.route.RoutePreviewFragment.REDUCE_TOLERANCE;
import static com.mapzen.support.TestHelper.getFixture;
import static com.mapzen.support.TestHelper.getTestLocation;
import static com.mapzen.support.TestHelper.getTestSimpleFeature;
import static com.mapzen.support.TestHelper.initBaseActivity;
import static com.mapzen.util.DouglasPeuckerReducer.reduceWithTolerance;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        activity.disableActionbar();
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
        assertThat(activity.getMapFragment().getView().findViewById(R.id.map)).isNotVisible();
        assertThat(activity.getMapFragment().getView().findViewById(R.id.progress)).isVisible();
    }

    @Test
    public void setRouteTo_successShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(activity.getMapFragment().getView().findViewById(R.id.map)).isVisible();
        assertThat(activity.getMapFragment().getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void setRouteTo_failureShouldDismissLoadingDialogUpon() throws Exception {
        fragment.createRouteToDestination();
        fragment.failure(500);
        assertThat(activity.getMapFragment().getView().findViewById(R.id.map)).isVisible();
        assertThat(activity.getMapFragment().getView().findViewById(R.id.progress)).isNotVisible();
    }

    @Test
    public void onStart_shouldHideActionbar() throws Exception {
        assertThat(activity.getActionBar()).isNotShowing();
    }

    @Test
    public void onDetach_shouldShowActionbar() throws Exception {
        fragment.onDetach();
        assertThat(activity.actionBarIsEnabled()).isTrue();
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
    public void onResume_shouldDeactivateMoveToMapUpdates() throws Exception {
        assertThat(((MapzenApplication) Robolectric.application)
                .shouldMoveMapToLocation()).isFalse();
    }

    @Test
    public void onDetach_shouldActivateMoveToMapUpdates() throws Exception {
        fragment.onDetach();
        assertThat(((MapzenApplication) Robolectric.application)
                .shouldMoveMapToLocation()).isTrue();
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
        verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] { 22.22, 44.44 });
        assertThat(values.get(1)).isEqualTo(new double[] { 1.0, 1.0 });
    }

    @Test
    public void createRouteToDestination_shouldGetFeatureDestinationFirst() throws Exception {
        getMapController().setLocation(getTestLocation(22.22, 44.44));
        fragment.reverse();
        verify(router, Mockito.times(2)).setLocation(location.capture());
        List<double[]> values = location.getAllValues();
        assertThat(values.get(0)).isEqualTo(new double[] { 1.0, 1.0 });
        assertThat(values.get(1)).isEqualTo(new double[] { 22.22, 44.44 });
    }

    @Test
    public void success_shouldAddPathToMap() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(path)).isTrue();
    }

    @Test
    public void success_shouldNotAddPathToMapMoreThanOnce() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        fragment.success(new Route(getFixture("around_the_block")));
        assertThat(getMapController().getMap().layers().contains(path)).isTrue();
    }

    @Test
    public void success_shouldClearPreviousRoutes() throws Exception {
        fragment.createRouteToDestination();
        fragment.success(new Route(getFixture("around_the_block")));
        verify(path).clearPath();
    }

    @Test
    public void failure_shouldClearPreviousRoutes() throws Exception {
        fragment.createRouteToDestination();
        fragment.failure(500);
        verify(path).clearPath();
    }

    @Test
    public void success_shouldDrawFullRoute() throws Exception {
        fragment.createRouteToDestination();
        Route route = new Route(getFixture("under_hundred"));
        fragment.success(route);
        for (Location loc : route.getGeometry()) {
            verify(path).addPoint(locationToGeoPoint(loc));
        }
    }

    @Test
    public void success_shouldDrawReducedRoute() throws Exception {
        fragment.createRouteToDestination();
        Route route = new Route(getFixture("ny_to_vermont"));
        fragment.success(route);

        for (Location loc : reduceWithTolerance(route.getGeometry(), REDUCE_TOLERANCE)) {
            verify(path).addPoint(locationToGeoPoint(loc));
        }
    }

    @Test
    public void shouldDefaultToRouteByCare() throws Exception {
        RadioButton byCar = (RadioButton) fragment.getView().findViewById(R.id.by_car);
        assertThat(byCar).isChecked();
    }

    @Test
    public void routeForCar_shouldRouteByCar() throws Exception {
        RadioButton byCar = (RadioButton) fragment.getView().findViewById(R.id.by_car);
        byCar.setChecked(false);
        byCar.performClick();
        verify(router).setDriving();
    }

    @Test
    public void routeForFoot_shouldRouteByFoot() throws Exception {
        fragment.getView().findViewById(R.id.by_foot).performClick();
        verify(router).setWalking();
    }

    @Test
    public void routeForBike_shouldRouteByBike() throws Exception {
        fragment.getView().findViewById(R.id.by_bike).performClick();
        verify(router).setBiking();
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
        verify(markers).removeAllItems();
    }

    @Test
    public void success_shouldAddBubblesAandB() throws Exception {
        Route testRoute = new Route(getFixture("around_the_block"));
        ArrayList<Location> geometry = testRoute.getGeometry();
        fragment.createRouteToDestination();
        fragment.success(testRoute);
        verify(markers, Mockito.times(2)).addItem(marker.capture());
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
    public void start_shouldStartRouting() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        ImageButton startBtn = (ImageButton) fragment.getView().findViewById(R.id.routing_circle);
        startBtn.performClick();
        assertThat(activity.getSupportFragmentManager()).hasFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void start_shouldNotStartRouting() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.reverse();
        ImageButton startBtn = (ImageButton) fragment.getView().findViewById(R.id.routing_circle);
        startBtn.performClick();
        assertThat(activity.getSupportFragmentManager()).
                doesNotHaveFragmentWithTag(RouteFragment.TAG);
    }

    @Test
    public void start_shouldClearBubbles() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        ImageButton startBtn = (ImageButton) fragment.getView().findViewById(R.id.routing_circle);
        startBtn.performClick();
        assertThat(getMapController().getMap().layers().contains(markers)).isFalse();
    }

    @Test
    public void reverse_shouldSetCircleButtonToView() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.reverse();
        ImageButton startBtn = (ImageButton) fragment.getView().findViewById(R.id.routing_circle);
        assertThat(startBtn.getTag()).isEqualTo(activity.getString(R.string.view));
    }

    @Test
    public void reverse_shouldToggleCircleButtonToStart() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.reverse();
        fragment.reverse();
        ImageButton startBtn = (ImageButton) fragment.getView().findViewById(R.id.routing_circle);
        assertThat(startBtn.getTag()).isEqualTo(activity.getString(R.string.start));
    }

    @Test
    public void onDetach_shouldRemoveMarkers() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.onDetach();
        assertThat(getMapController().getMap().layers().contains(markers)).isFalse();
    }

    @Test
    public void onDetach_shouldRemovePath() throws Exception {
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.onDetach();
        assertThat(getMapController().getMap().layers().contains(path)).isFalse();
    }

    @Test
    public void onDetach_shouldRedrawMap() throws Exception {
        MapFragment mockMapFragment = mock(MapFragment.class);
        fragment.createRouteToDestination();
        Route testRoute = new Route(getFixture("around_the_block"));
        fragment.success(testRoute);
        fragment.setMapFragment(mockMapFragment);
        fragment.onDetach();
        verify(mockMapFragment).updateMap();
    }

    @Test
    public void onViewCreate_shouldSetViewUpdateReceiver() throws Exception {
        ShadowApplication application = Robolectric.getShadowApplication();
        assertThat(application.hasReceiverForIntent(new Intent(COM_MAPZEN_UPDATE_VIEW))).isTrue();
    }

    @Test
    public void onDetach_shouldRemoveViewUpdateReceiver() throws Exception {
        fragment.onDetach();
        ShadowApplication application = Robolectric.getShadowApplication();
        assertThat(application.hasReceiverForIntent(new Intent(COM_MAPZEN_UPDATE_VIEW))).isFalse();
    }

    @Test
    public void onUpdateView_shouldCreateRoute() throws Exception {
        fragment.onViewUpdate();
        Mockito.verify(router).fetch();
    }
}

package com.mapzen.open.core;

import com.mapzen.open.MapController;
import com.mapzen.open.MapControllerTest;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.activity.BaseActivityTest;
import com.mapzen.open.activity.InitialActivity;
import com.mapzen.open.activity.InitialActivityTest;
import com.mapzen.open.activity.LoginActivity;
import com.mapzen.open.activity.LoginActivityTest;
import com.mapzen.open.adapters.PlaceArrayAdapter;
import com.mapzen.open.adapters.PlaceArrayAdapterTest;
import com.mapzen.android.Pelias;
import com.mapzen.open.fragment.ItemFragment;
import com.mapzen.open.fragment.ItemFragmentTest;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.fragment.MapFragmentTest;
import com.mapzen.osrm.Router;
import com.mapzen.open.route.DrawPathTask;
import com.mapzen.open.route.DrawPathTaskTest;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RouteFragmentTest;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.route.RoutePreviewFragmentTest;
import com.mapzen.open.search.AutoCompleteAdapter;
import com.mapzen.open.search.AutoCompleteAdapterTest;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.search.PagerResultsFragmentTest;
import com.mapzen.open.support.TestBaseActivity;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.mockito.Mockito;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import android.content.Context;
import android.graphics.Typeface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.mapzen.osrm.Router.getRouter;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@Module(
        injects = {
                InitialActivity.class,
                InitialActivityTest.class,
                TestBaseActivity.class,
                BaseActivity.class,
                BaseActivityTest.class,
                LoginActivity.class,
                LoginActivityTest.class,
                RouteFragment.class,
                RouteFragmentTest.class,
                ItemFragment.class,
                ItemFragmentTest.class,
                MapFragment.class,
                MapFragmentTest.class,
                RoutePreviewFragment.class,
                RoutePreviewFragmentTest.class,
                DataUploadService.class,
                PlaceArrayAdapter.class,
                PlaceArrayAdapterTest.class,
                AutoCompleteAdapter.class,
                AutoCompleteAdapterTest.class,
                MapzenLocation.class,
                MapzenLocationTest.class,
                MapController.class,
                MapControllerTest.class,
                DrawPathTask.class,
                DrawPathTaskTest.class,
                MapzenLocation.ConnectionCallbacks.class,
                MapzenLocation.Listener.class,
                PagerResultsFragment.class,
                PagerResultsFragmentTest.class
        },
        complete = false
)
public class TestAppModule {
    Context context;

    public TestAppModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton Router provideRouter() {
        Router router = Mockito.spy(getRouter());
        doNothing().when(router).fetch();
        return router;
    }

    @Provides OAuthRequestFactory provideOAuthRequestFactory() {
        return new TestOAuthRequestFactory();
    }

    @Provides @Singleton PathLayer providePathLayer() {
        return mock(PathLayer.class);
    }

    @Provides @Singleton ItemizedLayer<MarkerItem> provideItemizedLayer() {
        return mock(ItemizedLayer.class);
    }

    @Provides @Singleton MixpanelAPI provideMixpanelApi() {
        return mock(MixpanelAPI.class);
    }

    @Provides @Singleton Typeface provideTypeface() {
        return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");
    }

    @Provides @Singleton MapController provideMapController() {
        return MapController.getMapController();
    }

    @Provides @Singleton Pelias providePelias() {
        return Mockito.mock(Pelias.class);
    }

    @Provides @Singleton StyleDownLoader provideStyleDownloader() {
        return Mockito.mock(StyleDownLoader.class);
    }
}

package com.mapzen.core;

import com.mapzen.fragment.ItemFragment;
import com.mapzen.fragment.ItemFragmentTest;
import com.mapzen.fragment.MapFragment;
import com.mapzen.osrm.Router;
import com.mapzen.route.RouteFragment;
import com.mapzen.route.RoutePreviewFragment;
import com.mapzen.route.RoutePreviewFragmentTest;
import com.mapzen.support.TestBaseActivity;

import org.mockito.Mockito;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.mapzen.osrm.Router.getRouter;

@Module(
        injects = {
                TestBaseActivity.class,
                RouteFragment.class,
                ItemFragment.class,
                ItemFragmentTest.class,
                MapFragment.class,
                RoutePreviewFragment.class,
                RoutePreviewFragmentTest.class
        },
        complete = false
)
public class TestAppModule {
    Context context;

    public TestAppModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton Router provideRouter() {
        return Mockito.spy(getRouter());
    }

    @Provides @Singleton PathLayer providePathLayer() {
        return Mockito.mock(PathLayer.class);
    }

    @Provides @Singleton
    ItemizedLayer<MarkerItem> provideItemizedLayer() {
        return Mockito.mock(ItemizedLayer.class);
    }

}

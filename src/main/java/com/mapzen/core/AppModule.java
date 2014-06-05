package com.mapzen.core;

import com.mapzen.MapController;
import com.mapzen.activity.BaseActivity;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.osrm.Router;
import com.mapzen.route.RouteFragment;
import com.mapzen.route.RoutePreviewFragment;
import com.mapzen.util.MapzenProgressDialogFragment;

import org.oscim.backend.canvas.Color;
import org.oscim.layers.PathLayer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                BaseActivity.class,
                ItemFragment.class,
                RouteFragment.class,
                RoutePreviewFragment.class
        },
        complete = false,
        library = true
)
public class AppModule {
    @Provides @Singleton MapzenProgressDialogFragment provideMapzenProgressDialogFragment() {
        return new MapzenProgressDialogFragment();
    }

    @Provides @Singleton Router provideRouter() {
        return Router.getRouter();
    }

    @Provides @Singleton
    PathLayer providePathLayer() {
        return new PathLayer(MapController.getMapController().getMap(), Color.DKGRAY, 8);
    }
}

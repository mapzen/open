package com.mapzen.core;

import com.mapzen.activity.BaseActivity;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.osrm.Router;
import com.mapzen.route.RouteFragment;
import com.mapzen.route.RoutePreviewFragment;
import com.mapzen.util.MapzenProgressDialogFragment;

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
}

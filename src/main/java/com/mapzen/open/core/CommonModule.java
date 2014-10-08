package com.mapzen.open.core;

import com.mapzen.open.MapzenApplication;
import com.mapzen.android.lost.LocationClient;
import com.mapzen.helpers.ZoomController;
import com.mapzen.open.route.RouteEngine;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true)
public class CommonModule {
    private final MapzenApplication application;

    public CommonModule(MapzenApplication application) {
        this.application = application;
    }

    @Provides @Singleton LocationClient provideLocationClient() {
        MapzenLocation.ConnectionCallbacks callbacks =
                new MapzenLocation.ConnectionCallbacks(application);
        LocationClient locationClient = new LocationClient(application, callbacks);
        callbacks.setLocationClient(locationClient);
        return locationClient;
    }

    @Provides @Singleton ZoomController provideZoomController() {
        return new ZoomController();
    }

    @Provides @Singleton RouteEngine provideRouteEngine() {
        return new RouteEngine();
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }
}

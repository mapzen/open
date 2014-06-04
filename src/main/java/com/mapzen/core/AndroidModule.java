package com.mapzen.core;

import com.mapzen.MapzenApplication;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(library = true)
public class AndroidModule {
    private final MapzenApplication application;

    public AndroidModule(MapzenApplication application) {
        this.application = application;
    }

    /**
    * Allow the application context to be injected but require that it be annotated with
    * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
    */
    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }
}

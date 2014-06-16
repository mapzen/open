package com.mapzen.core;

import com.mapzen.MapzenApplication;
import com.mapzen.util.MapzenProgressDialogFragment;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(library = true)
public class CommonModule {
    private final MapzenApplication application;

    public CommonModule(MapzenApplication application) {
        this.application = application;
    }

    @Provides @Singleton MapzenProgressDialogFragment provideMapzenProgressDialogFragment() {
        return new MapzenProgressDialogFragment();
    }

    /**
    * Allow the application context to be injected but require that it be annotated with
    * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
    */
    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }
}

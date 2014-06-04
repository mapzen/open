package com.mapzen.core;

import com.mapzen.fragment.ItemFragment;
import com.mapzen.fragment.ItemFragmentTest;
import com.mapzen.fragment.MapFragment;
import com.mapzen.osrm.Router;
import com.mapzen.route.RouteFragment;
import com.mapzen.route.RoutePreviewFragment;
import com.mapzen.route.RoutePreviewFragmentTest;
import com.mapzen.support.TestBaseActivity;
import com.mapzen.util.MapzenProgressDialogFragment;

import org.mockito.Mockito;

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
    @Provides @Singleton MapzenProgressDialogFragment provideMapzenProgressDialogFragment() {
        return new MapzenProgressDialogFragment();
    }

    @Provides @Singleton Router provideRouter() {
        return Mockito.spy(getRouter());
    }
}

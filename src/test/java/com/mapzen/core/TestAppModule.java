package com.mapzen.core;

import com.mapzen.activity.BaseActivity;
import com.mapzen.activity.BaseActivityTest;
import com.mapzen.activity.InitialActivity;
import com.mapzen.activity.InitialActivityTest;
import com.mapzen.activity.LoginActivity;
import com.mapzen.activity.LoginActivityTest;
import com.mapzen.adapters.PlaceArrayAdapter;
import com.mapzen.adapters.PlaceArrayAdapterTest;
import com.mapzen.fragment.ItemFragment;
import com.mapzen.fragment.ItemFragmentTest;
import com.mapzen.fragment.MapFragment;
import com.mapzen.osrm.Router;
import com.mapzen.route.RouteFragment;
import com.mapzen.route.RouteFragmentTest;
import com.mapzen.route.RoutePreviewFragment;
import com.mapzen.route.RoutePreviewFragmentTest;
import com.mapzen.search.AutoCompleteAdapter;
import com.mapzen.support.TestBaseActivity;

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

@Module(
        injects = {
                TestBaseActivity.class,
                InitialActivity.class,
                InitialActivityTest.class,
                BaseActivity.class,
                BaseActivityTest.class,
                LoginActivity.class,
                LoginActivityTest.class,
                RouteFragment.class,
                RouteFragmentTest.class,
                ItemFragment.class,
                ItemFragmentTest.class,
                MapFragment.class,
                RoutePreviewFragment.class,
                RoutePreviewFragmentTest.class,
                DataUploadService.class,
                PlaceArrayAdapter.class,
                PlaceArrayAdapterTest.class,
                AutoCompleteAdapter.class
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
        return Mockito.mock(PathLayer.class);
    }

    @Provides @Singleton ItemizedLayer<MarkerItem> provideItemizedLayer() {
        return Mockito.mock(ItemizedLayer.class);
    }

    @Provides @Singleton Typeface provideTypeface() {
        return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");
    }
}

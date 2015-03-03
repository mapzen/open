package com.mapzen.open.core;

import com.mapzen.android.Pelias;
import com.mapzen.open.MapController;
import com.mapzen.open.MapControllerTest;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.MapzenApplicationTest;
import com.mapzen.open.TestMapzenApplication;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.activity.BaseActivityTest;
import com.mapzen.open.activity.InitialActivity;
import com.mapzen.open.activity.InitialActivityTest;
import com.mapzen.open.adapters.PlaceArrayAdapter;
import com.mapzen.open.adapters.PlaceArrayAdapterTest;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.fragment.MapFragmentTest;
import com.mapzen.open.login.LoginActivity;
import com.mapzen.open.login.LoginActivityTest;
import com.mapzen.open.route.DrawPathTask;
import com.mapzen.open.route.DrawPathTaskTest;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RouteFragmentTest;
import com.mapzen.open.route.RouteLocationIndicatorFactory;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.route.RoutePreviewFragmentTest;
import com.mapzen.open.search.AutoCompleteAdapter;
import com.mapzen.open.search.AutoCompleteAdapterTest;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.search.PagerResultsFragmentTest;
import com.mapzen.open.support.TestBaseActivity;
import com.mapzen.open.support.TestRouteLocationIndicatorFactory;
import com.mapzen.open.util.DatabaseHelper;
import com.mapzen.open.util.DebugDataSubmitter;
import com.mapzen.open.util.DebugDataSubmitterTest;
import com.mapzen.open.util.Logger;
import com.mapzen.open.util.LoggerTest;
import com.mapzen.open.util.SimpleCrypt;
import com.mapzen.osrm.Router;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.mockito.Mockito;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.mapzen.osrm.Router.getRouter;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
                MapFragment.class,
                MapFragmentTest.class,
                RoutePreviewFragment.class,
                RoutePreviewFragmentTest.class,
                DataUploadService.class,
                DataUploadServiceTest.class,
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
                MapzenLocation.Listener.class,
                PagerResultsFragment.class,
                PagerResultsFragmentTest.class,
                MapzenApplication.class,
                MapzenApplicationTest.class,
                TestMapzenApplication.class,
                DebugDataSubmitter.class,
                DebugDataSubmitterTest.class,
                Logger.class,
                LoggerTest.class
        },
        complete = false
)
public class TestAppModule {
    private static boolean simpleCryptEnabled = true;

    Context context;

    public TestAppModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton MapzenApplication provideApplication() {
        return (MapzenApplication) context.getApplicationContext();
    }

    @Provides @Singleton Router provideRouter() {
        Router router = Mockito.spy(getRouter());
        doNothing().when(router).fetch();
        return router;
    }

    @Provides OAuthRequestFactory provideOAuthRequestFactory() {
        return new TestOAuthRequestFactory();
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

    @Provides @Singleton SimpleCrypt provideSimpleCrypt() {
        if (!simpleCryptEnabled) {
            return null;
        }

        SimpleCrypt simpleCrypt = mock(SimpleCrypt.class);
        when(simpleCrypt.encode(anyString())).thenReturn("stuff");
        when(simpleCrypt.decode(anyString())).thenReturn("stuff");
        return simpleCrypt;
    }

    @Provides @Singleton SQLiteDatabase provideDb() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.enableWriteAheadLogging();
        return db;
    }

    @Provides @Singleton RouteLocationIndicatorFactory provideRouteLocationIndicatorFactory() {
        return new TestRouteLocationIndicatorFactory();
    }

    public static void setSimpleCryptEnabled(boolean enabled) {
        TestAppModule.simpleCryptEnabled = enabled;
    }
}

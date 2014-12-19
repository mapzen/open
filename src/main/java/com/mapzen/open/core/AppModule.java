package com.mapzen.open.core;

import com.mapzen.android.Pelias;
import com.mapzen.open.MapController;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.activity.InitialActivity;
import com.mapzen.open.adapters.PlaceArrayAdapter;
import com.mapzen.open.fragment.ItemFragment;
import com.mapzen.open.fragment.MapFragment;
import com.mapzen.open.login.LoginActivity;
import com.mapzen.open.route.DrawPathTask;
import com.mapzen.open.route.RouteFragment;
import com.mapzen.open.route.RoutePreviewFragment;
import com.mapzen.open.search.AutoCompleteAdapter;
import com.mapzen.open.search.PagerResultsFragment;
import com.mapzen.open.util.DatabaseHelper;
import com.mapzen.open.util.DebugDataSubmitter;
import com.mapzen.open.util.Logger;
import com.mapzen.open.util.SimpleCrypt;
import com.mapzen.osrm.Router;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Color;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                InitialActivity.class,
                BaseActivity.class,
                LoginActivity.class,
                ItemFragment.class,
                RouteFragment.class,
                RoutePreviewFragment.class,
                DataUploadService.class,
                PlaceArrayAdapter.class,
                AutoCompleteAdapter.class,
                MapzenLocation.class,
                MapFragment.class,
                MapController.class,
                DrawPathTask.class,
                MapzenLocation.ConnectionCallbacks.class,
                MapzenLocation.Listener.class,
                PagerResultsFragment.class,
                MapzenApplication.class,
                DebugDataSubmitter.class,
                Logger.class
        },
        complete = false,
        library = true
)
public class AppModule {
    Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton Router provideRouter() {
        return Router.getRouter();
    }

    @Provides OAuthRequestFactory provideOAuthRequestFactory() {
        return new OAuthRequestFactory();
    }

    @Provides PathLayer providePathLayer() {
        return new PathLayer(MapController.getMapController().getMap(), Color.DKGRAY, 8);
    }

    @Provides @Singleton ItemizedLayer<MarkerItem> provideItemizedLayer() {
        return new ItemizedLayer<MarkerItem>(
                MapController.getMapController().getMap(), new ArrayList<MarkerItem>(),
                AndroidGraphics.makeMarker(context.getResources().getDrawable(R.drawable.ic_pin),
                MarkerItem.HotspotPlace.BOTTOM_CENTER), null);
    }

    @Provides @Singleton Typeface provideTypeface() {
        return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");
    }

    @Provides @Singleton MixpanelAPI provideMixpanelApi() {
        return MixpanelAPI.getInstance(context, context.getString(R.string.mixpanel_token));
    }

    @Provides @Singleton MapController provideMapController() {
        return MapController.getMapController();
    }

    @Provides @Singleton Pelias providePelias() {
        return Pelias.getPelias();
    }

    @Provides @Singleton StyleDownLoader provideStyleDownloader() {
        StyleDownLoader styleDownLoader = new StyleDownLoader(context);
        styleDownLoader.setHost("http://vector-styles.mapzen.com/");
        return styleDownLoader;
    }

    @Provides @Singleton SimpleCrypt provideSimpleCrypt() {
        if ("bogus_key".equals(context.getString(R.string.osm_key))) {
            return null;
        }

        return new SimpleCrypt();
    }

    @Provides @Singleton SQLiteDatabase provideDb() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.enableWriteAheadLogging();
        return db;
    }
}

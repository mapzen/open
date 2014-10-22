package com.mapzen.open;

import com.mapzen.open.core.CommonModule;
import com.mapzen.open.core.AppModule;
import com.mapzen.open.core.OSMApi;
import com.mapzen.open.util.DatabaseHelper;
import com.mapzen.open.util.SimpleCrypt;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application {
    private ObjectGraph graph;
    private boolean moveMapLocation = false;

    protected List<Object> getModules() {
        return Arrays.asList(
                new CommonModule(this),
                new AppModule(this)
        );
    }

    public void inject(Object object) {
        graph.inject(object);
    }

    public static final String PELIAS_BLOB = "text";
    private final String[] columns = {
            _ID, PELIAS_BLOB
    };
    public static final String LOG_TAG = "Mapzen: ";
    private String currentSearchTerm = "";
    private SQLiteDatabase db;
    private OAuthService osmOauthService;
    @Inject SimpleCrypt simpleCrypt;

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules().toArray());
        inject(this);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();
        db.enableWriteAheadLogging();
        osmOauthService = new ServiceBuilder()
                .provider(OSMApi.class)
                .apiKey(simpleCrypt.decode(getString(R.string.osm_key)))
                .debug()
                .callback("mapzen://oauth-login/mapzen.com")
                .apiSecret(simpleCrypt.decode(getString(R.string.osm_secret))).build();
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    public void setCurrentSearchTerm(String currentSearchTerm) {
        this.currentSearchTerm = currentSearchTerm;
    }

    public Token getAccessToken() {
        Token accessToken = null;
        SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        if (!prefs.getString("token", "").isEmpty()) {
            accessToken = new Token(simpleCrypt.decode(prefs.getString("token", "")),
                    simpleCrypt.decode(prefs.getString("secret", "")));
        }
        return accessToken;
    }

    public boolean isLoggedIn() {
        Token accessToken = getAccessToken();
        return accessToken != null;
    }

    public boolean wasForceLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        return prefs.getBoolean("forced_login", false);
    }

    public OAuthService getOsmOauthService() {
        return osmOauthService;
    }

    public void setOsmOauthService(OAuthService service) {
        this.osmOauthService = service;
    }

    public boolean shouldMoveMapToLocation() {
        return moveMapLocation;
    }

    public void deactivateMoveMapToLocation() {
        moveMapLocation = false;
    }

    public void activateMoveMapToLocation() {
        moveMapLocation = true;
    }

    public void setAccessToken(Token accessToken) {
        SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", simpleCrypt.encode(accessToken.getToken()));
        editor.putString("secret", simpleCrypt.encode(accessToken.getSecret()));
        editor.commit();
    }
}

package com.mapzen;

import com.mapzen.core.AndroidModule;
import com.mapzen.core.AppModule;
import com.mapzen.core.OSMApi;
import com.mapzen.util.DatabaseHelper;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

import static android.provider.BaseColumns._ID;

public class MapzenApplication extends Application {
    private ObjectGraph graph;

    protected List<Object> getModules() {
        return Arrays.asList(
                new AndroidModule(this),
                new AppModule()
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

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules().toArray());
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        db = databaseHelper.getWritableDatabase();
        db.enableWriteAheadLogging();
        osmOauthService = new ServiceBuilder()
                .provider(OSMApi.class)
                .apiKey(getString(R.string.osm_key))
                .debug()
                .callback("mapzen://oauth-login/mapzen.com")
                .apiSecret(getString(R.string.osm_secret)).build();
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
            accessToken = new Token(prefs.getString("token", ""), prefs.getString("secret", ""));
        }
        return accessToken;
    }

    public boolean isLoggedIn() {
        Token accessToken = getAccessToken();
        return accessToken != null;
    }

    public OAuthService getOsmOauthService() {
        return osmOauthService;
    }

    public void setOsmOauthService(OAuthService service) {
        this.osmOauthService = service;
    }

    public void setAccessToken(Token accessToken) {
        SharedPreferences prefs = getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", accessToken.getToken());
        editor.putString("secret", accessToken.getSecret());
        editor.commit();
    }
}

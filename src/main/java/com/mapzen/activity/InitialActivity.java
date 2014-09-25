package com.mapzen.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mapzen.MapzenApplication;
import com.mapzen.util.MixpanelHelper;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;

import javax.inject.Inject;

import static com.mapzen.util.MixpanelHelper.Event.INITIAL_LAUNCH;
import static com.mapzen.util.MixpanelHelper.Payload.fromHashMap;

public class InitialActivity extends Activity {
    private MapzenApplication app;
    @Inject MixpanelAPI mixpanelAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MapzenApplication) getApplication();
        app.inject(this);
        mixpanelTrack();
        if (app.isLoggedIn() || app.wasForceLoggedIn()) {
            startBaseActivity();
        } else {
            startLoginActivity();
        }
    }

    private void mixpanelTrack() {
        HashMap<String, Object> payload = new HashMap<String, Object>(1);
        payload.put(MixpanelHelper.Payload.LOGGED_IN_KEY, app.isLoggedIn());
        mixpanelAPI.track(INITIAL_LAUNCH, fromHashMap(payload));
    }

    private void startBaseActivity() {
        Intent baseActivity = new Intent(this, BaseActivity.class);
        baseActivity.setData(getIntent().getData());
        startActivity(baseActivity);
        finish();
    }

    private void startLoginActivity() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        loginActivity.setData(getIntent().getData());
        startActivity(loginActivity);
        finish();
    }
}

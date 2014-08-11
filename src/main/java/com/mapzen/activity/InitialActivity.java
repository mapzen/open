package com.mapzen.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.mapzen.MapzenApplication;

public class InitialActivity extends Activity {
    private MapzenApplication app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MapzenApplication) getApplication();
        if (app.isLoggedIn() || app.wasForceLoggedIn()) {
            startBaseActivity();
        } else {
            startLoginActivity();
        }
    }

    private void startBaseActivity() {
        Intent baseActivity = new Intent(this, BaseActivity.class);
        startActivity(baseActivity);
        finish();
    }

    private void startLoginActivity() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }
}

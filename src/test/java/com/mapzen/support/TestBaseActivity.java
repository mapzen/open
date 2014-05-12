package com.mapzen.support;

import com.mapzen.activity.BaseActivity;
import com.mapzen.android.lost.LocationClient;

import org.oscim.map.Map;
import org.oscim.map.TestMap;

import android.app.ActionBar;

import java.util.concurrent.Executor;

public class TestBaseActivity extends BaseActivity {
    private ActionBar actionBar = new TestActionBar();
    private boolean backPressed = false;
    private boolean optionsMenuInvalidated = false;
    private Map map = new TestMap();
    private String debugDataEndpoint;

    @Override
    public ActionBar getActionBar() {
        return actionBar;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
    }

    public boolean isBackPressed() {
        return backPressed;
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        super.supportInvalidateOptionsMenu();
        optionsMenuInvalidated = true;
    }

    public boolean isOptionsMenuInvalidated() {
        return optionsMenuInvalidated;
    }

    @Override
    public Map getMap() {
        return map;
    }

    public LocationClient.ConnectionCallbacks getConnectionCallback() {
        return connectionCallback;
    }


    @Override
    public void initDebugDataSubmitter() {
        super.initDebugDataSubmitter();
        debugDataSubmitter.setEndpoint(debugDataEndpoint);
    }

    public void setDebugDataEndpoint(String debugDataEndpoint) {
        this.debugDataEndpoint = debugDataEndpoint;
    }

    public void setDebugDataExecutor(Executor executor) {
        debugDataExecutor = executor;
    }
}

package com.mapzen.support;

import android.app.ActionBar;

import com.mapzen.activity.BaseActivity;

import org.oscim.map.Map;
import org.oscim.map.TestMap;

public class TestBaseActivity extends BaseActivity {
    private ActionBar actionBar = new TestActionBar();
    private boolean backPressed = false;
    private boolean optionsMenuInvalidated = false;

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
        return new TestMap();
    }
}

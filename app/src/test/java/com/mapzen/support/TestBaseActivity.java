package com.mapzen.support;

import android.app.ActionBar;

import com.mapzen.activity.BaseActivity;

public class TestBaseActivity extends BaseActivity {
    private boolean backPressed = false;
    private ActionBar actionBar = new TestActionBar();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
    }

    @Override
    public ActionBar getActionBar() {
        return actionBar;
    }

    public boolean getBackPressed() {
        return backPressed;
    }
}

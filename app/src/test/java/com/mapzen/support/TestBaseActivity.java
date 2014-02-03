package com.mapzen.support;

import com.mapzen.activity.BaseActivity;

public class TestBaseActivity extends BaseActivity {
    private boolean backPressed = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
    }

    public boolean getBackPressed() {
        return backPressed;
    }
}

package com.mapzen.support;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.SpinnerAdapter;
import org.robolectric.Robolectric;

public class TestActionBar extends ActionBar {

    private CharSequence title;
    private boolean homeButtonEnabled = false;
    private boolean showHomeAsUp = false;
    private boolean isShowing = false;

    @Override
    public void setHomeButtonEnabled(boolean enabled) {
        homeButtonEnabled = enabled;
    }

    public boolean getHomeButtonEnabled() {
        return homeButtonEnabled;
    }

    @Override
    public void setCustomView(View view) {
    }

    @Override
    public void setCustomView(View view, LayoutParams layoutParams) {
    }

    @Override
    public void setCustomView(int resId) {
    }

    @Override
    public void setIcon(int resId) {
    }

    @Override
    public void setIcon(Drawable icon) {
    }

    @Override
    public void setLogo(int resId) {
    }

    @Override
    public void setLogo(Drawable logo) {
    }

    @Override
    public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {
    }

    @Override
    public void setSelectedNavigationItem(int position) {
    }

    @Override
    public int getSelectedNavigationIndex() {
        return 0;
    }

    @Override
    public int getNavigationItemCount() {
        return 0;
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Override
    public void setTitle(int resId) {
        this.title = Robolectric.application.getString(resId);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
    }

    @Override
    public void setSubtitle(int resId) {
    }

    @Override
    public void setDisplayOptions(int options) {
    }

    @Override
    public void setDisplayOptions(int options, int mask) {
    }

    @Override
    public void setDisplayUseLogoEnabled(boolean useLogo) {
    }

    @Override
    public void setDisplayShowHomeEnabled(boolean showHome) {
    }

    @Override
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        this.showHomeAsUp = showHomeAsUp;
    }

    public boolean getDisplayHomeAsUpEnabled() {
        return showHomeAsUp;
    }

    @Override
    public void setDisplayShowTitleEnabled(boolean showTitle) {
    }

    @Override
    public void setDisplayShowCustomEnabled(boolean showCustom) {
    }

    @Override
    public void setBackgroundDrawable(Drawable d) {
    }

    @Override
    public View getCustomView() {
        return null;
    }

    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public CharSequence getSubtitle() {
        return null;
    }

    @Override
    public int getNavigationMode() {
        return 0;
    }

    @Override
    public void setNavigationMode(int mode) {
    }

    @Override
    public int getDisplayOptions() {
        return 0;
    }

    @Override
    public Tab newTab() {
        return null;
    }

    @Override
    public void addTab(Tab tab) {
    }

    @Override
    public void addTab(Tab tab, boolean setSelected) {
    }

    @Override
    public void addTab(Tab tab, int position) {
    }

    @Override
    public void addTab(Tab tab, int position, boolean setSelected) {
    }

    @Override
    public void removeTab(Tab tab) {
    }

    @Override
    public void removeTabAt(int position) {
    }

    @Override
    public void removeAllTabs() {
    }

    @Override
    public void selectTab(Tab tab) {
    }

    @Override
    public Tab getSelectedTab() {
        return null;
    }

    @Override
    public Tab getTabAt(int index) {
        return null;
    }

    @Override
    public int getTabCount() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void show() {
        isShowing = true;
    }

    @Override
    public void hide() {
        isShowing = false;
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
    }

    @Override
    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
    }
}
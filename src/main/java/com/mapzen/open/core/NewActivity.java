package com.mapzen.open.core;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;
import com.mapzen.open.scenes.AutoCompleteSearchScene;

import com.davidstemmer.screenplay.SimpleActivityDirector;
import com.davidstemmer.screenplay.flow.Screenplay;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import javax.inject.Inject;

import flow.Flow;

public class NewActivity extends ActionBarActivity {

    @Inject SimpleActivityDirector director;
    @Inject Screenplay screenplay;
    @Inject Flow flow;
    @Inject AutoCompleteSearchScene searchScene;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.main);
        ((MapzenApplication) getApplication()).inject(this);
        director.bind(this, relativeLayout);
        screenplay.enter(flow);
    }

    @Override public void onBackPressed() {
        if (!flow.goBack()) {
            super.onBackPressed();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        director.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        searchView.setIconified(false);
                        flow.goTo(searchScene);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        flow.goBack();
                        return true;
                    }
                });

        return true;
    }
}

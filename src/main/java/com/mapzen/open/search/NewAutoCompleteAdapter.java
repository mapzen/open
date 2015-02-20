package com.mapzen.open.search;

import com.mapzen.android.Pelias;
import com.mapzen.open.MapzenApplication;
import com.mapzen.open.R;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.graphics.Typeface;
import android.widget.ArrayAdapter;

import java.util.Arrays;

import javax.inject.Inject;

public class NewAutoCompleteAdapter extends ArrayAdapter<String> {
    public static final int AUTOCOMPLETE_THRESHOLD = 3;
    private final MapzenApplication app;
    @Inject Typeface typeface;
    @Inject Pelias pelias;
    @Inject MixpanelAPI mixpanelApi;
    @Inject SavedSearch savedSearch;

    private static final String[] FAKE_QUERIES = {"pizza", "deli", "Yankee Stadium"};

    @Inject
    public NewAutoCompleteAdapter(MapzenApplication app) {
        super(app, R.layout.search_dropdown_item, Arrays.asList(FAKE_QUERIES));
        this.app = app;
    }
}

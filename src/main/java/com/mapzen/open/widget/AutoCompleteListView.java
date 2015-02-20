package com.mapzen.open.widget;

import com.mapzen.open.MapzenApplication;
import com.mapzen.open.search.NewAutoCompleteAdapter;
import com.mapzen.open.search.SavedSearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.ListView;

import javax.inject.Inject;

public class AutoCompleteListView extends ListView {
    @Inject SharedPreferences prefs;
    @Inject Resources res;
    @Inject NewAutoCompleteAdapter adapter;
    @Inject SavedSearch savedSearch;


    public AutoCompleteListView(Context context) {
        super(context);
    }

    public AutoCompleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ((MapzenApplication) getContext().getApplicationContext()).inject(this);
        setAdapter(adapter);
    }
}

package com.mapzen.open.search;

import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class PeliasSearchView extends SearchView {
    public PeliasSearchView(Context context) {
        super(context);
    }

    public PeliasSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PeliasSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAutoCompleteListView(final ListView listView) {
        setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {
                final int visibility = hasFocus ? VISIBLE : GONE;
                listView.setVisibility(visibility);
            }
        });
    }
}

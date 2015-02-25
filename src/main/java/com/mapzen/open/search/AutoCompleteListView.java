package com.mapzen.open.search;

import com.mapzen.open.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class AutoCompleteListView extends ListView {
    private View headerView;

    public AutoCompleteListView(Context context) {
        super(context);
    }

    public AutoCompleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void showHeader() {
        if (getHeaderViewsCount() == 0) {
            headerView = inflate(getContext(), R.layout.recent_search_list_header, null);
            addHeaderView(headerView);
            setHeaderDividersEnabled(false);
        }
    }

    public void hideHeader() {
        removeHeaderView(headerView);
    }
}

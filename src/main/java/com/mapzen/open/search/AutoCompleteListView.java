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
        init();
    }

    public AutoCompleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoCompleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        headerView = inflate(getContext(), R.layout.recent_search_list_header, null);
        addHeaderView(headerView);
        setHeaderDividersEnabled(false);
    }

    public void showHeader() {
        headerView.findViewById(R.id.recent_search_header).setVisibility(VISIBLE);
    }

    public void hideHeader() {
        headerView.findViewById(R.id.recent_search_header).setVisibility(GONE);
    }

    public boolean isHeaderVisible() {
        return headerView.findViewById(R.id.recent_search_header).getVisibility() == VISIBLE;
    }
}

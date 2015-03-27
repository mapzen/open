package com.mapzen.open.search;

import com.mapzen.open.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AutoCompleteListView extends ListView {
    private View headerView;
    private View emptyView;

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

    @Override
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        updateEmptyView();
    }

    @Override
    public View getEmptyView() {
        return emptyView;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        updateEmptyView();
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        if (emptyView == null) {
            return;
        }

        if (getVisibility() != VISIBLE) {
            emptyView.setVisibility(View.GONE);
            return;
        }

        if (getAdapter() == null || getAdapter().isEmpty()) {
            emptyView.setVisibility(VISIBLE);
        } else {
            emptyView.setVisibility(GONE);
        }
    }
}

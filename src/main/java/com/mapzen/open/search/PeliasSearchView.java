package com.mapzen.open.search;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SearchView;

public class PeliasSearchView extends SearchView {
    private static int searchIcon;
    private static int closeIcon;

    public static void setSearchIcon(int resId) {
        searchIcon = resId;
    }

    public static void setCloseIcon(int resId) {
        closeIcon = resId;
    }

    public PeliasSearchView(Context context) {
        this(context, null);
    }

    public PeliasSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSearchIcon();
        initCloseIcon();
    }

    private void initSearchIcon() {
        if (searchIcon == 0) {
            return;
        }

        final ImageView close = (ImageView) findViewById(getResources()
                .getIdentifier("android:id/search_mag_icon", null, null));
        close.setImageDrawable(getResources().getDrawable(searchIcon));
    }

    private void initCloseIcon() {
        if (closeIcon == 0) {
            return;
        }

        final ImageView close = (ImageView) findViewById(getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        close.setImageDrawable(getResources().getDrawable(closeIcon));
    }
}

package com.mapzen.open.search;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.widget.ImageView;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class PeliasSearchViewTest {
    private PeliasSearchView peliasSearchView;

    @Before
    public void setUp() throws Exception {
        peliasSearchView = new PeliasSearchView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(peliasSearchView).isNotNull();
    }

    @Test
    public void setSearchIcon_shouldSetCustomDrawable() throws Exception {
        PeliasSearchView.setSearchIcon(R.drawable.ic_search);
        peliasSearchView = new PeliasSearchView(application);
        final ImageView icon = (ImageView) peliasSearchView.findViewById(application.getResources()
                .getIdentifier("android:id/search_mag_icon", null, null));
        assertThat(icon.getDrawable()).isEqualTo(application.getResources()
                .getDrawable(R.drawable.ic_search));
    }

    @Test
    public void setCloseIcon_shouldSetCustomDrawable() throws Exception {
        PeliasSearchView.setCloseIcon(R.drawable.ic_cancel);
        peliasSearchView = new PeliasSearchView(application);
        final ImageView icon = (ImageView) peliasSearchView.findViewById(application.getResources()
                .getIdentifier("android:id/search_close_btn", null, null));
        assertThat(icon.getDrawable()).isEqualTo(application.getResources()
                .getDrawable(R.drawable.ic_cancel));
    }
}

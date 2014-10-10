package com.mapzen.open.login;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class LoginAdapterTest {
    private LoginAdapter loginAdapter;

    @Before
    public void setUp() throws Exception {
        loginAdapter = new LoginAdapter(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(loginAdapter).isNotNull();
    }

    @Test
    public void instantiateItem_shouldAddViewToContainer() throws Exception {
        ViewGroup container = new FrameLayout(application);
        loginAdapter.instantiateItem(container, 0);
        assertThat(container).hasChildCount(1);
    }

    @Test
    public void instantiateItem_shouldHaveAppLogo() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), 0);
        assertThat(view.findViewById(R.id.logo)).isNotNull();
    }

    @Test
    public void destroyItem_shouldRemoveViewFromContainer() throws Exception {
        View view = new View(application);
        ViewGroup container = new FrameLayout(application);
        container.addView(view);
        loginAdapter.destroyItem(container, 0, view);
        assertThat(container.getChildAt(0)).isNull();
    }

    @Test
    public void getCount_shouldReturnThree() throws Exception {
        assertThat(loginAdapter).hasCount(3);
    }

    @Test
    public void isViewFromObject_shouldReturnTrueIfSame() throws Exception {
        View view = new View(application);
        assertThat(loginAdapter.isViewFromObject(view, view)).isTrue();
    }
}

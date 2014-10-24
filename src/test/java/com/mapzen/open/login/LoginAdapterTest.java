package com.mapzen.open.login;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import static com.mapzen.open.login.LoginAdapter.PAGE_2;
import static com.mapzen.open.login.LoginAdapter.PAGE_3;
import static com.mapzen.open.login.LoginAdapter.PAGE_4;
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

    @Test
    public void instantiateItem_pageTwoShouldHaveLogo() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_2);
        assertThat(view.findViewById(R.id.logo)).isNotNull();
    }

    @Test
    public void instantiateItem_pageTwoShouldHaveTitle() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_2);
        TextView intro = (TextView) view.findViewById(R.id.title);
        assertThat(intro).hasText(R.string.login_page_two_title);
    }

    @Test
    public void instantiateItem_pageTwoShouldHaveBody() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_2);
        TextView body = (TextView) view.findViewById(R.id.body);
        assertThat(body).hasText(R.string.login_page_two_body);
    }

    @Test
    public void instantiateItem_pageThreeShouldHaveLogo() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_3);
        assertThat(view.findViewById(R.id.logo)).isNotNull();
    }

    @Test
    public void instantiateItem_pageThreeShouldHaveTitle() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_3);
        TextView intro = (TextView) view.findViewById(R.id.title);
        assertThat(intro).hasText(R.string.login_page_three_title);
    }

    @Test
    public void instantiateItem_pageThreeShouldHaveBody() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_3);
        TextView body = (TextView) view.findViewById(R.id.body);
        assertThat(body).hasText(R.string.login_page_three_body);
    }

    @Test
    public void instantiateItem_pageThreeShouldHaveLearnMoreLink() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_3);
        TextView learnMore = (TextView) view.findViewById(R.id.learn_more);
        assertThat(learnMore).hasText(R.string.login_page_three_learn_more);
    }

    @Test
    public void instantiateItem_pageFourShouldHaveBody() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_4);
        TextView body = (TextView) view.findViewById(R.id.body);
        assertThat(body).hasText(R.string.login_page_four_body);
    }

    @Test
    public void instantiateItem_pageFourShouldHaveLoginButton() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_4);
        Button loginButton = (Button) view.findViewById(R.id.login_button);
        assertThat(loginButton).hasText(R.string.login_page_four_button);
    }

    @Test
    public void instantiateItem_learnMoreLinkShouldStartBrowserWithWebsite() throws Exception {
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_3);
        TextView learnMore = (TextView) view.findViewById(R.id.learn_more);
        learnMore.performClick();
        Intent next = Robolectric.getShadowApplication().getNextStartedActivity();
        assertThat(next).hasAction(Intent.ACTION_VIEW);
        assertThat(next).hasData("https://mapzen.com");
    }

    @Test
    public void instantiateItem_loginButtonShouldInvokeCallback() throws Exception {
        TestLoginListener listener = new TestLoginListener();
        loginAdapter.setLoginListener(listener);
        View view = (View) loginAdapter.instantiateItem(new FrameLayout(application), PAGE_4);
        Button loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.performClick();
        assertThat(listener.login).isTrue();
    }

    private class TestLoginListener implements LoginAdapter.LoginListener {
        private boolean login;

        @Override
        public void doLogin() {
            login = true;
        }
    }
}

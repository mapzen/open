package com.mapzen.open.util;

import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotificationManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class NotificationBroadcastReceiverTest {
    private NotificationBroadcastReceiver receiver;
    private NotificationManager notificationManager;
    private ShadowNotificationManager shadowNotificationManager;

    @Before
    public void setUp() throws Exception {
        receiver = new NotificationBroadcastReceiver();
        notificationManager = (NotificationManager)
                application.getSystemService(NOTIFICATION_SERVICE);
        shadowNotificationManager = shadowOf(notificationManager);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(receiver).isNotNull();
    }

    @Test
    public void onReceive_shouldCancelAllNotifications() throws Exception {
        notificationManager.notify(0, new Notification());
        receiver.onReceive(application, createFakeExitNavigationIntent());
        assertThat(shadowNotificationManager.getAllNotifications()).hasSize(0);
    }

    @Test
    public void onReceive_shouldStartBaseActivityWithExitIntent() throws Exception {
        receiver.onReceive(application, createFakeExitNavigationIntent());
        Intent intent = getShadowApplication().getNextStartedActivity();
        assertThat(intent).hasComponent(application, BaseActivity.class);
        assertThat(intent).hasExtra(MapzenNotificationCreator.EXIT_NAVIGATION);
    }

    private Intent createFakeExitNavigationIntent() {
        final Intent intent = new Intent();
        intent.putExtra(MapzenNotificationCreator.EXIT_NAVIGATION, true);
        return intent;
    }
}

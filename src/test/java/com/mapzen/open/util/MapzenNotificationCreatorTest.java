package com.mapzen.open.util;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowNotificationManager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(MapzenTestRunner.class)
public class MapzenNotificationCreatorTest {
    private static final Activity ACTIVITY = buildActivity(Activity.class).create().get();

    private MapzenNotificationCreator mnc;

    @Before
    public void setUp() throws Exception {
        mnc = new MapzenNotificationCreator(ACTIVITY);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(mnc).isNotNull();
    }

    @Test
    public void shouldCreatePersistentNotification() throws Exception {
        mnc.createNewNotification("title", "content");
        NotificationManager notificationManager = (NotificationManager)
                ACTIVITY.getSystemService(Robolectric.application.NOTIFICATION_SERVICE);
        ShadowNotificationManager shadowManager = Robolectric.shadowOf(notificationManager);
        Notification notification = shadowManager.getAllNotifications().get(0);
        assertThat(Robolectric.shadowOf(notification).isOngoing()).isTrue();
    }
}

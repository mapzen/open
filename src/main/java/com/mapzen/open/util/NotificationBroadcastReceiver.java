package com.mapzen.open.util;

import com.mapzen.open.activity.BaseActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(MapzenNotificationCreator.EXIT_NAVIGATION, false)) {
            cancelAllNotifications(context);
            startBaseActivityWithExitExtra(context);
        }
    }

    private void cancelAllNotifications(Context context) {
        final NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Activity.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void startBaseActivityWithExitExtra(Context context) {
        final Intent exitRoutingIntent = new Intent(context, BaseActivity.class);
        exitRoutingIntent.putExtra(MapzenNotificationCreator.EXIT_NAVIGATION, true);
        exitRoutingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(exitRoutingIntent);
    }
}

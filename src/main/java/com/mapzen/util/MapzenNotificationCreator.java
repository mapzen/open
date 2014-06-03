package com.mapzen.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.mapzen.R;
import com.mapzen.activity.BaseActivity;


public class MapzenNotificationCreator {
    public static final String exitNavigation = "exit_navigation";
    private Activity baseActivity;
    private NotificationCompat.Builder builder;
    private NotificationCompat.BigTextStyle bigTextStyle;
    private TaskStackBuilder stackBuilder;
    private Intent notificationIntent, exitNavigationIntent;
    private PendingIntent pendingNotificationIntent, pendingExitNavigationIntent;
    private NotificationManager mNotificationManager;

    public MapzenNotificationCreator(Activity act) {
        baseActivity = act;
        mNotificationManager = (NotificationManager) baseActivity.getSystemService(
                baseActivity.getApplicationContext().NOTIFICATION_SERVICE);
    }

    public void createNewNotification(String title, String content) {
        initBuilder(title, content);
        initBigTextStyle(title, content);
        builder.setStyle(bigTextStyle);
        initNotificationIntent();
        initExitNavigationIntent();
        initStackBuilder(notificationIntent);
        builder.addAction(R.drawable.ic_dismiss, "Exit Navigation", pendingExitNavigationIntent);
        builder.setContentIntent(pendingNotificationIntent.getActivity(baseActivity.getBaseContext(), 0, notificationIntent, 0));
        mNotificationManager.notify(0, builder.build());
    }

    private void initExitNavigationIntent() {
        exitNavigationIntent = new Intent(baseActivity, NotificationBroadcastReciever.class);
        exitNavigationIntent.putExtra(exitNavigation, true);
        pendingExitNavigationIntent = PendingIntent.getBroadcast(baseActivity, 0, exitNavigationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initNotificationIntent() {
        notificationIntent = new Intent(baseActivity, BaseActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        pendingNotificationIntent = PendingIntent.getActivity(baseActivity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initStackBuilder(Intent intent) {
        stackBuilder = TaskStackBuilder.create(baseActivity);
        stackBuilder.addParentStack(baseActivity.getClass());
        stackBuilder.addNextIntent(intent);
    }

    private void initBigTextStyle(String title, String content) {
        bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(content);
    }

    private void initBuilder(String title, String content) {
        builder = new NotificationCompat.Builder(baseActivity.getBaseContext());
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_lets_go);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
    }
}








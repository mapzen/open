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

    public void createNewNotifiction(String title, String content, Activity act) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(act);
        NotificationCompat.BigTextStyle bigTestStyle = new NotificationCompat.BigTextStyle();
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content);
        bigTestStyle.setBigContentTitle(title);
        bigTestStyle.bigText(content);
        mBuilder.setStyle(bigTestStyle);
        mBuilder.setSmallIcon(R.drawable.ic_lets_go);

        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        Intent intent = new Intent(act, BaseActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(act, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(act);
        stackBuilder.addParentStack(act.getClass());
        stackBuilder.addNextIntent(intent);
        mBuilder.addAction(R.drawable.ic_dismiss,"Exit Navigation", pIntent);

        intent.setFlags(Intent.| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mBuilder.setContentIntent(pIntent);

        NotificationManager mNotificationManager = (NotificationManager) act.getSystemService(act.getApplicationContext().NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

}

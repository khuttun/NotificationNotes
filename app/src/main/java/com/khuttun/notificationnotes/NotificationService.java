package com.khuttun.notificationnotes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * NotificationService displays and cancels notifications.
 */
public class NotificationService extends IntentService
{
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    // Names for extras
    public static final String ID = "ID";
    public static final String SHOW = "SHOW";
    public static final String TITLE = "TITLE";
    public static final String TEXT = "TEXT";

    public NotificationService()
    {
        super("NotificationService");
        Log.d(Globals.TAG, "NotificationService ctor");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(Globals.TAG, "NotificationService onCreate");

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.notificationBuilder = new NotificationCompat.Builder(this);
        this.notificationBuilder.setSmallIcon(R.drawable.pen);
        this.notificationBuilder.setOngoing(true);
        this.notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        this.notificationBuilder.setContentIntent(
            PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        int id = intent.getIntExtra(ID, -1);
        boolean show = intent.getBooleanExtra(SHOW, false);
        Log.d(Globals.TAG, "NotificationService: " + id + " - " + show);

        if (show)
        {
            this.notificationBuilder.setContentTitle(intent.getStringExtra(TITLE));
            this.notificationBuilder.setContentText(intent.getStringExtra(TEXT));
            this.notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(intent.getStringExtra(TEXT)));
            Notification notification = this.notificationBuilder.build();
            this.notificationManager.notify(id, notification);
        }
        else
        {
            this.notificationManager.cancel(id);
        }
    }
}

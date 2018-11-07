package com.khuttun.notificationnotes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.ArrayList;

/**
 * NotificationService displays and cancels notifications.
 */
public class NotificationService extends IntentService
{
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private static final String CHANNEL_ID = "notes";

    // Intent extras names and values
    private static final String ID = "com.khuttun.notificationnotes.ID";
    private static final String GROUP_NOTES = "com.khuttun.notificationnotes.ALL_NOTES";
    private static final String SHOW = "com.khuttun.notificationnotes.SHOW";
    private static final String TITLE = "com.khuttun.notificationnotes.TITLE";
    private static final String TEXT = "com.khuttun.notificationnotes.TEXT";
    private static final int GROUP_NOTIF_ID = -1000;
    private static final int CLEAR_ALL_ID = -2000;

    public static void setNotification(Context context, NotificationNote note)
    {
        context.startService(new Intent(context, NotificationService.class)
                .putExtra(ID, note.id)
                .putExtra(SHOW, note.isVisible)
                .putExtra(TITLE, note.title)
                .putExtra(TEXT, note.text));
    }

    public static void setGroupNotification(Context context, ArrayList<NotificationNote> notes)
    {
        ArrayList<Bundle> groupNotes = new ArrayList<>();
        for (int i = 0; i < notes.size(); ++i)
        {
            NotificationNote note = notes.get(i);
            if (note.isVisible)
            {
                Bundle b = new Bundle();
                b.putString(TITLE, note.title);
                b.putString(TEXT, note.text);
                groupNotes.add(b);
            }
        }

        context.startService(new Intent(context, NotificationService.class)
                .putExtra(ID, GROUP_NOTIF_ID)
                .putParcelableArrayListExtra(GROUP_NOTES, groupNotes));
    }

    public static void clearAllNotifications(Context context)
    {
        context.startService(new Intent(context, NotificationService.class).putExtra(ID, CLEAR_ALL_ID));
    }

    public NotificationService()
    {
        super("NotificationService");
        if (Globals.LOG) Log.d(Globals.TAG, "NotificationService ctor");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (Globals.LOG) Log.d(Globals.TAG, "NotificationService onCreate");

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // NotificationChannel is required on Oreo and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            this.notificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW));
        }

        this.notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
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

        if (id == CLEAR_ALL_ID)
        {
            if (Globals.LOG) Log.d(Globals.TAG, "Clearing all notifications");
            this.notificationManager.cancelAll();
        }
        else if (id == GROUP_NOTIF_ID)
        {
            ArrayList<Bundle> notes = intent.getParcelableArrayListExtra(GROUP_NOTES);
            if (Globals.LOG) Log.d(Globals.TAG, "Group notification: size " + notes.size());
            switch (notes.size())
            {
                case 0:
                    this.notificationManager.cancel(GROUP_NOTIF_ID);
                    break;

                case 1:
                    this.notificationManager.notify(
                            GROUP_NOTIF_ID,
                            createNotification(notes.get(0).getString(TITLE), notes.get(0).getString(TEXT)));
                    break;

                default:
                    this.notificationManager.notify(GROUP_NOTIF_ID, createGroupNotification(notes));
                    break;
            }
        }
        else
        {
            boolean show = intent.getBooleanExtra(SHOW, false);
            if (Globals.LOG) Log.d(Globals.TAG, "Notification: ID " + id + ", show " + show);
            if (show)
            {
                this.notificationManager.notify(
                        id,
                        createNotification(intent.getStringExtra(TITLE), intent.getStringExtra(TEXT)));
            }
            else
            {
                this.notificationManager.cancel(id);
            }
        }
    }

    private Notification createNotification(String title, String text)
    {
        this.notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        this.notificationBuilder.setContentTitle(title);
        this.notificationBuilder.setContentText(text);
        return this.notificationBuilder.build();
    }

    private Notification createGroupNotification(ArrayList<Bundle> notes)
    {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (int i = 0; i < notes.size(); ++i)
        {
            style.addLine(getGroupNotificationLine(notes.get(i)));
        }
        this.notificationBuilder.setStyle(style);
        this.notificationBuilder.setContentTitle(getString(R.string.group_notif_title) + ": " + notes.size());
        this.notificationBuilder.setContentText(getGroupNotificationLine(notes.get(0)));
        return this.notificationBuilder.build();
    }

    private CharSequence getGroupNotificationLine(Bundle note)
    {
        String title = note.getString(TITLE);
        String text = note.getString(TEXT);
        Spannable sp = new SpannableString(title + " " + text);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }
}

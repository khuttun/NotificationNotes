package com.khuttun.notificationnotes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.ArrayList;

/**
 * NotificationMgr handles displaying the note notifications.
 */
public class NotificationMgr
{
    private static final String CHANNEL_ID = "notes";
    private static final int GROUP_NOTIF_ID = -1000;

    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public NotificationMgr(Context context)
    {
        this.context = context;
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        // NotificationChannel is required on Oreo and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            this.notificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID,
                    this.context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW));
        }

        this.notificationBuilder = new NotificationCompat.Builder(this.context, CHANNEL_ID);
        this.notificationBuilder.setSmallIcon(R.drawable.pen);
        this.notificationBuilder.setOngoing(true);
        this.notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        this.notificationBuilder.setContentIntent(PendingIntent
                .getActivity(this.context, 0, new Intent(this.context, MainActivity.class), 0));
    }

    public void setNotification(NotificationNote note)
    {
        if (Globals.LOG) Log.d(Globals.TAG, "Notification: ID " + note.id + ", show " + note.isVisible);
        if (note.isVisible)
        {
            this.notificationManager.notify(note.id, createNotification(note.title, note.text));
        }
        else
        {
            this.notificationManager.cancel(note.id);
        }
    }

    public void setGroupNotification(ArrayList<NotificationNote> notes)
    {
        ArrayList<NotificationNote> visibleNotes = new ArrayList<>();
        for (int i = 0; i < notes.size(); ++i)
        {
            NotificationNote n = notes.get(i);
            if (n.isVisible)
            {
                visibleNotes.add(n);
            }
        }

        if (Globals.LOG) Log.d(Globals.TAG, "Group notification: visible notes " + visibleNotes.size());

        switch (visibleNotes.size())
        {
            case 0:
                this.notificationManager.cancel(GROUP_NOTIF_ID);
                break;

            case 1:
                this.notificationManager.notify(
                        GROUP_NOTIF_ID,
                        createNotification(visibleNotes.get(0).title, visibleNotes.get(0).text));
                break;

            default:
                this.notificationManager.notify(GROUP_NOTIF_ID, createGroupNotification(visibleNotes));
                break;
        }
    }

    public void clearAllNotifications()
    {
        if (Globals.LOG) Log.d(Globals.TAG, "Clearing all notifications");
        this.notificationManager.cancelAll();
    }

    private Notification createNotification(String title, String text)
    {
        this.notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        this.notificationBuilder.setContentTitle(title);
        this.notificationBuilder.setContentText(text);
        return this.notificationBuilder.build();
    }

    private Notification createGroupNotification(ArrayList<NotificationNote> notes)
    {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (int i = 0; i < notes.size(); ++i)
        {
            style.addLine(getGroupNotificationLine(notes.get(i)));
        }
        this.notificationBuilder.setStyle(style);
        this.notificationBuilder.setContentTitle(
                this.context.getString(R.string.group_notif_title) + ": " + notes.size());
        this.notificationBuilder.setContentText(getGroupNotificationLine(notes.get(0)));
        return this.notificationBuilder.build();
    }

    private CharSequence getGroupNotificationLine(NotificationNote note)
    {
        Spannable sp = new SpannableString(note.title + " " + note.text);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, note.title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }
}

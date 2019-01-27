package com.khuttun.notificationnotes;

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
import android.support.v4.app.NotificationManagerCompat;
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
    private static final int SUMMARY_NOTIF_ID = -2000;
    private static final String GROUP_KEY = "com.khuttun.notificationnotes";
    private Context context;
    private NotificationManager notificationManager;
    private boolean groupAPI = false;
    private NotificationCompat.Builder notificationBuilder;

    public NotificationMgr(Context context)
    {
        this.context = context;
        // Need the NotificationManager type for a NotificationChannel,
        // the official documentation saying to use NotificationManager.Compat is
        // wrong
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        // A NotificationChannel is needed for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            this.notificationManager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID,
                    this.context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW));
        }

        // Grouping notifications together so they are expandable is available on Nougat and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            this.groupAPI = true;
        }

        notificationBuilder = new NotificationCompat.Builder(this.context, CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.pen);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        notificationBuilder.setContentIntent(PendingIntent
                .getActivity(this.context, 0, new Intent(this.context, MainActivity.class), 0));
    }

    /**
     * Used for group mode to check if a notification that is
     * currently being displayed needs to be canceled
     */
    public void checkNotification(NotificationNote note)
    {
        if (!note.isVisible)
        {
            this.notificationManager.cancel(note.id);
        }
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
        ArrayList<NotificationNote> visibleNotes = getVisibleNotes(notes);
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
                if (groupAPI)
                {
                    displayGroupNotifications(visibleNotes);
                }
                else
                {
                    this.notificationManager.notify(SUMMARY_NOTIF_ID,
                            createSummaryNotification(visibleNotes));
                }
                break;
        }
    }

    private static ArrayList<NotificationNote> getVisibleNotes(ArrayList<NotificationNote> notes)
    {
        ArrayList<NotificationNote> visibleNotes = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++)
        {
            NotificationNote n = notes.get(i);
            if (n.isVisible)
            {
                visibleNotes.add(n);
            }
        }
        return visibleNotes;
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

    /**
     * Creates a notification with the number of notes and the first line of each note.
     * This notification is shown for Android Versions below Nougat, and is still necessary
     * for Android Nougat, but is not shown.
     */
    private Notification createSummaryNotification(ArrayList<NotificationNote> notes)
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
        this.notificationBuilder.setGroup(GROUP_KEY);
        this.notificationBuilder.setGroupSummary(true);
        return this.notificationBuilder.build();
    }

    private CharSequence getGroupNotificationLine(NotificationNote note)
    {
        Spannable sp = new SpannableString(note.title + " " + note.text);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, note.title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    private void displayGroupNotifications(ArrayList<NotificationNote> visibleNotes)
    {
        int visibleSize = visibleNotes.size();
        for (int i = 0; i < visibleSize; i++)
        {
            NotificationNote note = visibleNotes.get(i);
            if (Globals.LOG)
            {
                Log.d(Globals.TAG, "Displaying notification for note with id ".concat(Integer.toString(note.id)));
            }
            this.notificationManager.notify(note.id, createGroupNotification(note));
        }
        this.notificationManager.notify(SUMMARY_NOTIF_ID, createSummaryNotification(visibleNotes));
    }

    /**
     * Creates a notification that is the member of a group & is not the summary.
     */
    private Notification createGroupNotification(NotificationNote note)
    {
        this.notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(note.text));
        this.notificationBuilder.setContentTitle(note.title);
        this.notificationBuilder.setContentText(note.text);
        this.notificationBuilder.setGroup(this.GROUP_KEY);
        this.notificationBuilder.setGroupSummary(false);
        return this.notificationBuilder.build();
    }
}


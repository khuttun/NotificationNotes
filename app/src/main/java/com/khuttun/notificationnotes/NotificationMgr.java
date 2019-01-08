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
    private static  final  int SUMMARY_NOTIF_ID = -2000;
    private static final String GROUP_KEY = "com.khuttun.notificationnotes";
    private Context context;
    private NotificationManager notificationManager;
    // private NotificationCompat.Builder notificationBuilder;
    private boolean groupAPI = false;

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

        // Grouping notifications together so they are expandable is available on Nougat and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            this.groupAPI = true;
        }
    }

    public NotificationCompat.Builder getNotificationBuilder()
    {
        NotificationCompat.Builder Builder = new NotificationCompat.Builder(this.context, CHANNEL_ID);
        Builder.setSmallIcon(R.drawable.pen);
        Builder.setOngoing(true);
        Builder.setPriority(NotificationCompat.PRIORITY_LOW);
        Builder.setContentIntent(PendingIntent
                .getActivity(this.context, 0, new Intent(this.context, MainActivity.class), 0));
        return Builder;
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
                    ArrayList<Notification> groupedNotes = createGroupNotifications(visibleNotes);
                    for (int i = 0; i < groupedNotes.size(); i++)
                    {
                        Notification note = groupedNotes.get(i);
                        String title = note.extras.getString("android.title");
                        String text = note.extras.getString("android.text");
                        int id = i == groupedNotes.size()-1 ?
                                SUMMARY_NOTIF_ID : findNotification(notes, title, text);
                        this.notificationManager.notify("note", id, note);
                    }
                }
                else
                {
                    this.notificationManager.notify(GROUP_NOTIF_ID,
                            createLegacyGroupNotification(visibleNotes));
                }
                break;
        }
    }

    private int findNotification(ArrayList<NotificationNote> notes, String title, String text)
    {
        for (int j = 0; j < notes.size(); j++)
        {
            NotificationNote note = notes.get(j);
            if (note.text == text && note.title == title)
            {
                return note.id;
            }
        }
        return -1;
    }

    private ArrayList<NotificationNote> getVisibleNotes(ArrayList<NotificationNote> notes) {
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
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        return notificationBuilder.build();
    }


    // Used for Android versions below Nougat (7.0)
    private Notification createLegacyGroupNotification(ArrayList<NotificationNote> notes)
    {
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (int i = 0; i < notes.size(); ++i)
        {
            style.addLine(getGroupNotificationLine(notes.get(i)));
        }
        notificationBuilder.setStyle(style);
        notificationBuilder.setContentTitle(
                this.context.getString(R.string.group_notif_title) + ": " + notes.size());
        notificationBuilder.setContentText(getGroupNotificationLine(notes.get(0)));
        return notificationBuilder.build();
    }


    private CharSequence getGroupNotificationLine(NotificationNote note)
    {
        Spannable sp = new SpannableString(note.title + " " + note.text);
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, note.title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    // Used for Android Nougat (7.0) and above
    private ArrayList<Notification> createGroupNotifications(ArrayList<NotificationNote> notes) {

        ArrayList<Notification> groupedNotes = new ArrayList<Notification>();
        for (int i = 0; i < notes.size(); ++i)
        {
            NotificationNote note = notes.get(i);
            Notification newNote = createGroupNotification(note.title, note.text);
            groupedNotes.add(newNote);
        }
        Notification summary = createSummaryNotification(notes.size());
        groupedNotes.add(summary);
        return groupedNotes;
    }

    private Notification createGroupNotification(String title, String text)
    {
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setGroup(this.GROUP_KEY);
        notificationBuilder.setGroupSummary(false);
        return notificationBuilder.build();
    }

    private Notification createSummaryNotification(int size)
    {
        String message = this.context.getString(R.string.group_notif_title) + ": " + size;
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setStyle(new NotificationCompat.InboxStyle());
        notificationBuilder.setContentTitle(message);
        notificationBuilder.setContentText(message);
        notificationBuilder.setGroup(this.GROUP_KEY);
        notificationBuilder.setGroupSummary(true);
        return notificationBuilder.build();
    }
}


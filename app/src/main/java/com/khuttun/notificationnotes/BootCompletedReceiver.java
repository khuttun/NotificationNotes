package com.khuttun.notificationnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Receive boot completed action
 */
public class BootCompletedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String notesJson = context.getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
            .getString(Globals.NOTES_PREF_NAME, "[]");
        Log.d(Globals.TAG, "Boot completed: " + notesJson);

        ArrayList<NotificationNote> noteList = Globals.jsonToNoteList(notesJson);
        for (int i = 0; i < noteList.size(); ++i)
        {
            NotificationNote n = noteList.get(i);
            if (n.isVisible)
            {
                context.startService(
                    new Intent(context, NotificationService.class)
                        .putExtra(NotificationService.ID, n.id)
                        .putExtra(NotificationService.SHOW, n.isVisible)
                        .putExtra(NotificationService.TITLE, n.title)
                        .putExtra(NotificationService.TEXT, n.text));
            }
        }
    }
}

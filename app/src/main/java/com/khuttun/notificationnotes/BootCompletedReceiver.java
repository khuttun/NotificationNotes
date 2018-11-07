package com.khuttun.notificationnotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
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
        // TODO: Notification doesn't appear because of restrictions introduced in Oreo:
        // https://developer.android.com/about/versions/oreo/background

        ArrayList<NotificationNote> notes = Globals.jsonToNoteList(PreferenceManager
                .getDefaultSharedPreferences(context).getString(Globals.NOTES_PREF_NAME, "[]"));

        if (Globals.LOG) Log.d(Globals.TAG, "Boot completed, " + notes.size() + " notes");

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context
                .getString(R.string.group_notif_pref_key), false))
        {
            NotificationService.setGroupNotification(context, notes);
        }
        else
        {
            for (int i = 0; i < notes.size(); ++i)
            {
                NotificationNote n = notes.get(i);
                if (n.isVisible)
                {
                    NotificationService.setNotification(context, n);
                }
            }
        }
    }
}

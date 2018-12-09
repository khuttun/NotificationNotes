package com.khuttun.notificationnotes;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private NotificationMgr notificationMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Globals.LOG) Log.d(Globals.TAG, "Creating SettingsActivity");
        setContentView(R.layout.activity_settings);
        this.notificationMgr = new NotificationMgr(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (Globals.LOG) Log.d(Globals.TAG, "Pausing SettingsActivity");
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Globals.LOG) Log.d(Globals.TAG, "Resuming SettingsActivity");
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
    {
        if (key.equals(getString(R.string.group_notif_pref_key)))
        {
            boolean settingVal = sharedPrefs.getBoolean(key, false);
            if (Globals.LOG) Log.d(Globals.TAG, "Group notifications setting changed to " + settingVal);

            // Clear all existing notifications and set them again using the new setting value
            this.notificationMgr.clearAllNotifications();
            ArrayList<NotificationNote> notes = Globals.jsonToNoteList(PreferenceManager
                    .getDefaultSharedPreferences(this).getString(Globals.NOTES_PREF_NAME, "[]"));
            if (settingVal)
            {
                this.notificationMgr.setGroupNotification(notes);
            }
            else
            {
                for (int i = 0; i < notes.size(); ++i)
                {
                    NotificationNote n = notes.get(i);
                    if (n.isVisible)
                    {
                        this.notificationMgr.setNotification(n);
                    }
                }
            }
        }
    }
}

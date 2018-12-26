package com.khuttun.notificationnotes;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ThemedActivity extends AppCompatActivity
{
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.theme = getSelectedTheme();
        if (Globals.LOG) Log.d(Globals.TAG, "Creating ThemedActivity, theme " + this.theme);

        if (this.theme.equals(getString(R.string.theme_pref_value_light)))
        {
            setTheme(R.style.LightTheme);
        }
        if (this.theme.equals(getString(R.string.theme_pref_value_dark)))
        {
            setTheme(R.style.DarkTheme);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String selectedTheme = getSelectedTheme();
        if (!selectedTheme.equals(this.theme))
        {
            if (Globals.LOG) Log.d(Globals.TAG, "Theme has changed, recreating activity");
            recreate();
        }
    }

    private String getSelectedTheme()
    {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.theme_pref_key),
                getString(R.string.theme_pref_value_default));
    }
}

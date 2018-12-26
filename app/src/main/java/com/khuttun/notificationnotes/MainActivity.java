package com.khuttun.notificationnotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends ThemedActivity
{
    /**
     * Observe changes in notes list and display an alternate view if the list is empty.
     */
    private static class EmptyNoteListObserver extends RecyclerView.AdapterDataObserver
    {
        private RecyclerView noteList;
        private View alternateView;

        EmptyNoteListObserver(RecyclerView noteList, View alternateView)
        {
            this.noteList = noteList;
            this.alternateView = alternateView;
            this.noteList.getAdapter().registerAdapterDataObserver(this);
        }

        @Override
        public void onChanged()
        {
            if (Globals.LOG) Log.d(Globals.TAG, "List changed. Count " + noteList.getAdapter().getItemCount());
            if (noteList.getAdapter().getItemCount() > 0)
            {
                this.noteList.setVisibility(View.VISIBLE);
                this.alternateView.setVisibility(View.GONE);
            }
            else
            {
                this.noteList.setVisibility(View.GONE);
                this.alternateView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount)
        {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount)
        {
            onChanged();
        }
    }

    /**
     * AddNoteResult is used to store the result from "Add note" activity.
     */
    private static class AddNoteResult
    {
        public int reqCode;
        public String title;
        public String text;
        public int noteIndex;

        public AddNoteResult(int reqCode, String title, String text, int noteIndex)
        {
            this.reqCode = reqCode;
            this.title = title;
            this.text = text;
            this.noteIndex = noteIndex;
        }

        @Override
        public String toString()
        {
            return "{" + this.reqCode + ", " + this.title + ", " + this.text + ", " + this.noteIndex + "}";
        }
    }

    private NotesListAdapter notesListAdapter;
    private AddNoteResult addNoteResult;
    private EmptyNoteListObserver noteListObserver;

    public void addNote(View view)
    {
        startActivityForResult(new Intent(this, AddNoteActivity.class), AddNoteActivity.ADD_REQ);
    }

    public void deleteNote(int notePos)
    {
        this.notesListAdapter.deleteNote(notePos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (Globals.LOG) Log.d(Globals.TAG, "Req " + requestCode + ", result " + resultCode);
        if (resultCode == RESULT_OK)
        {
            this.addNoteResult = new AddNoteResult(
                    requestCode,
                    data.getStringExtra(AddNoteActivity.TITLE),
                    data.getStringExtra(AddNoteActivity.TEXT),
                    data.getIntExtra(AddNoteActivity.NOTE_INDEX, -1));
            if (Globals.LOG) Log.d(Globals.TAG, "Caching result: " + this.addNoteResult);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Globals.LOG) Log.d(Globals.TAG, "Creating MainActivity");
        setContentView(R.layout.activity_main);

        this.notesListAdapter = new NotesListAdapter(this, getSupportFragmentManager());

        RecyclerView noteListView = (RecyclerView) findViewById(R.id.notes_recycler_view);
        noteListView.setLayoutManager(new LinearLayoutManager(this));
        noteListView.setAdapter(this.notesListAdapter);

        this.addNoteResult = null;
        this.noteListObserver = new EmptyNoteListObserver(noteListView, findViewById(R.id.empty_text_view));

        migratePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                if (Globals.LOG) Log.d(Globals.TAG, "Settings menu item selected");
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (Globals.LOG) Log.d(Globals.TAG, "Pausing MainActivity");

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putString(Globals.NOTES_PREF_NAME, Globals.noteListToJson(this.notesListAdapter.getNotes()));
        prefs.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Globals.LOG) Log.d(Globals.TAG, "Resuming MainActivity");

        this.notesListAdapter.setNotes(Globals.jsonToNoteList(
                PreferenceManager.getDefaultSharedPreferences(this).getString(Globals.NOTES_PREF_NAME, "[]")));

        // Unprocessed result from "Add note" activity
        if (this.addNoteResult != null)
        {
            if (Globals.LOG) Log.d(Globals.TAG, "Result from AddNoteActivity: " + this.addNoteResult);
            switch (this.addNoteResult.reqCode)
            {
                case AddNoteActivity.ADD_REQ:
                    this.notesListAdapter.addNote(this.addNoteResult.title, this.addNoteResult.text);
                    break;

                case AddNoteActivity.EDIT_REQ:
                    this.notesListAdapter.updateNote(
                            this.addNoteResult.noteIndex, this.addNoteResult.title, this.addNoteResult.text);
                    break;
            }

            this.addNoteResult = null;
        }
    }

    private void migratePreferences()
    {
        // The storage of the notes has changed in version 1.6. Migrate old notes if found.
        String oldPref = getPreferences(Context.MODE_PRIVATE).getString(Globals.NOTES_PREF_NAME, null);
        if (oldPref != null)
        {
            if (Globals.LOG) Log.d(Globals.TAG, "Migrating old notes pref");

            // Take into account also possibility that there's already notes stored to the new storage
            String newPref = PreferenceManager.getDefaultSharedPreferences(this).getString(
                    Globals.NOTES_PREF_NAME,
                    "[]");

            // Combine old and new versions of the stored notes
            ArrayList<NotificationNote> notes = Globals.jsonToNoteList(oldPref);
            if (Globals.LOG) Log.d(Globals.TAG, notes.size() + " notes stored to old pref");
            notes.addAll(Globals.jsonToNoteList(newPref));
            if (Globals.LOG) Log.d(Globals.TAG, notes.size() + " notes stored total");

            // Store the combined notes
            SharedPreferences.Editor newPrefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            newPrefsEditor.putString(Globals.NOTES_PREF_NAME, Globals.noteListToJson(notes));
            newPrefsEditor.commit();

            // Remove the old version
            SharedPreferences.Editor oldPrefsEditor = getPreferences(Context.MODE_PRIVATE).edit();
            oldPrefsEditor.remove(Globals.NOTES_PREF_NAME);
            oldPrefsEditor.commit();
        }
    }
}

package com.khuttun.notificationnotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity
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
            Log.d(Globals.TAG, "List changed. Count " + noteList.getAdapter().getItemCount());
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
        public void	onItemRangeRemoved(int positionStart, int itemCount)
        {
            onChanged();
        }
    }

    private static final String NOTES_PREF_NAME = "notes";
    private static final int ADD_NOTE_REQ = 1;

    private NotesListAdapter notesListAdapter;

    // Data for new note to add
    private String titleToAdd = null;
    private String textToAdd = null;

    private EmptyNoteListObserver noteListObserver;

    public void addNote(View view)
    {
        startActivityForResult(new Intent(this, AddNoteActivity.class), ADD_NOTE_REQ);
    }

    public void deleteNote(int notePos)
    {
        this.notesListAdapter.deleteNote(notePos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(Globals.TAG, "Req " + requestCode + ", result " + resultCode);
        if (requestCode == ADD_NOTE_REQ && resultCode == RESULT_OK)
        {
            titleToAdd = data.getStringExtra("title");
            textToAdd = data.getStringExtra("text");
            Log.d(Globals.TAG, "Note to add: " + titleToAdd + ": " + textToAdd);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.notesListAdapter = new NotesListAdapter(this, getSupportFragmentManager());

        RecyclerView noteListView = (RecyclerView) findViewById(R.id.notes_recycler_view);
        noteListView.setLayoutManager(new LinearLayoutManager(this));
        noteListView.setAdapter(this.notesListAdapter);

        this.noteListObserver = new EmptyNoteListObserver(noteListView, findViewById(R.id.empty_text_view));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(Globals.TAG, "Pausing");

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
        prefs.putString(NOTES_PREF_NAME, Globals.noteListToJson(this.notesListAdapter.getNotes()));
        prefs.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(Globals.TAG, "Resuming");

        this.notesListAdapter.setNotes(Globals.jsonToNoteList(
            getPreferences(Context.MODE_PRIVATE).getString(NOTES_PREF_NAME, "[]")));

        // Add note if there is something to add
        if (titleToAdd != null && textToAdd != null)
        {
            Log.d(Globals.TAG, "Found note to add: " + titleToAdd + " - " + textToAdd);
            notesListAdapter.addNote(titleToAdd, textToAdd);
            titleToAdd = null;
            textToAdd = null;
        }
    }
}

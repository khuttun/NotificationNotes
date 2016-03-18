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
        Log.d(Globals.TAG, "Req " + requestCode + ", result " + resultCode);
        if (resultCode == RESULT_OK)
        {
            this.addNoteResult = new AddNoteResult(
                requestCode,
                data.getStringExtra(AddNoteActivity.TITLE),
                data.getStringExtra(AddNoteActivity.TEXT),
                data.getIntExtra(AddNoteActivity.NOTE_INDEX, -1));
            Log.d(Globals.TAG, "Caching result: " + this.addNoteResult.title + ": " + this.addNoteResult.text);
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

        this.addNoteResult = null;
        this.noteListObserver = new EmptyNoteListObserver(noteListView, findViewById(R.id.empty_text_view));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(Globals.TAG, "Pausing");

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
        prefs.putString(Globals.NOTES_PREF_NAME, Globals.noteListToJson(this.notesListAdapter.getNotes()));
        prefs.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(Globals.TAG, "Resuming");

        this.notesListAdapter.setNotes(Globals.jsonToNoteList(
            getPreferences(Context.MODE_PRIVATE).getString(Globals.NOTES_PREF_NAME, "[]")));

        // Unprocessed result from "Add note" activity
        if (this.addNoteResult != null)
        {
            Log.d(Globals.TAG, "Result from AddNoteActivity: " + this.addNoteResult.reqCode + " - " +
                this.addNoteResult.title + " - " + this.addNoteResult.text + " - " + this.addNoteResult.noteIndex);

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
}

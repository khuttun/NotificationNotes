package com.khuttun.notificationnotes;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.pen);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);

        this.notesListAdapter = new NotesListAdapter(
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE),
            notificationBuilder);

        RecyclerView noteListView = (RecyclerView) findViewById(R.id.notes_recycler_view);
        noteListView.setLayoutManager(new LinearLayoutManager(this));
        noteListView.setAdapter(this.notesListAdapter);

        this.noteListObserver = new EmptyNoteListObserver(
            noteListView, findViewById(R.id.empty_text_view));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(Globals.TAG, "Pausing");

        JSONArray jsonArray = new JSONArray();
        ArrayList<NotificationNote> notes = this.notesListAdapter.getNotes();
        for (int i = 0; i < notes.size(); ++i)
        {
            JSONObject jsonObject = new JSONObject();
            NotificationNote n = notes.get(i);
            try
            {
                jsonObject.put("id", n.id);
                jsonObject.put("title", n.title);
                jsonObject.put("text", n.text);
                jsonObject.put("isVisible", n.isVisible);
                jsonArray.put(jsonObject);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        Log.d(Globals.TAG, jsonArray.toString());

        SharedPreferences.Editor prefs = getPreferences(Context.MODE_PRIVATE).edit();
        prefs.putString(NOTES_PREF_NAME, jsonArray.toString());
        prefs.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(Globals.TAG, "Resuming");

        ArrayList<NotificationNote> notes = new ArrayList<>();

        try
        {
            JSONArray jsonArray = new JSONArray(getPreferences(Context.MODE_PRIVATE).getString(NOTES_PREF_NAME, "[]"));
            Log.d(Globals.TAG, jsonArray.toString());
            for (int i = 0; i < jsonArray.length(); ++i)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                NotificationNote n = new NotificationNote(jsonObject.getInt("id"), jsonObject.getString("title"),
                    jsonObject.getString("text"), jsonObject.getBoolean("isVisible"));
                notes.add(n);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        this.notesListAdapter.setNotes(notes);

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

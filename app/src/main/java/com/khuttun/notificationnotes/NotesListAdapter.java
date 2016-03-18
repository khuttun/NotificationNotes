package com.khuttun.notificationnotes;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for the main NotificationNote list.
 */
class NotesListAdapter
    extends RecyclerView.Adapter<NotesListAdapter.ViewHolder>
    implements NotesController
{
    private Activity context;
    private FragmentManager fragmentManager;
    private ArrayList<NotificationNote> notes;

    /**
     * ViewHolder holds the view showing one NotificationNote.
     * It also listens to UI actions in the view and forwards the actions to NotesController.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener
    {
        public TextView titleView;
        public TextView textView;
        public SwitchCompat switchView;
        private NotesController notesController;
        private FragmentManager fragmentManager;

        public ViewHolder(View v, NotesController notesController, FragmentManager fragmentManager)
        {
            super(v);

            this.titleView = (TextView) v.findViewById(R.id.note_title);
            this.textView = (TextView) v.findViewById(R.id.note_text);
            this.switchView = (SwitchCompat) v.findViewById(R.id.note_switch);
            this.notesController = notesController;
            this.fragmentManager = fragmentManager;

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            this.switchView.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v)
        {
            this.notesController.onNoteClicked(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v)
        {
            DeleteNoteDialogFragment.newInstance(
                getAdapterPosition(), this.titleView.getText().toString()).show(
                this.fragmentManager, "deleteNoteDialog");
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            this.notesController.onNoteCheckedChanged(getAdapterPosition(), isChecked);
        }
    }

    public NotesListAdapter(Activity context, FragmentManager fragmentManager)
    {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.notes = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false);
        ViewHolder vh = new ViewHolder(v, this, this.fragmentManager);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        NotificationNote n = this.notes.get(position);
        holder.titleView.setText(n.title);
        holder.textView.setText(n.text);
        holder.switchView.setChecked(n.isVisible);
    }

    @Override
    public int getItemCount()
    {
        return this.notes.size();
    }

    @Override
    public void onNoteClicked(int position)
    {
        Log.d(Globals.TAG, "Note clicked " + position);
        NotificationNote n = this.notes.get(position);
        Intent in = new Intent(this.context, AddNoteActivity.class);
        in.putExtra(AddNoteActivity.TITLE, n.title);
        in.putExtra(AddNoteActivity.TEXT, n.text);
        in.putExtra(AddNoteActivity.NOTE_INDEX, position);
        context.startActivityForResult(in, AddNoteActivity.EDIT_REQ);
    }

    @Override
    public void onNoteCheckedChanged(int position, boolean isChecked)
    {
        NotificationNote n = this.notes.get(position);
        if (isChecked != n.isVisible)
        {
            n.isVisible = isChecked;
            setNotification(n);
        }
    }

    public ArrayList<NotificationNote> getNotes()
    {
        return this.notes;
    }

    public void setNotes(ArrayList<NotificationNote> notes)
    {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void addNote(String title, String text)
    {
        NotificationNote n = new NotificationNote(getId(), title, text, true);
        this.notes.add(n);
        notifyItemInserted(this.notes.size() - 1);
        setNotification(n);
    }

    public void deleteNote(int position)
    {
        NotificationNote n = this.notes.get(position);
        this.notes.remove(position);
        notifyItemRemoved(position);
        n.isVisible = false;
        setNotification(n);
    }

    public void updateNote(int position, String title, String text)
    {
        NotificationNote n = this.notes.get(position);
        n.title = title;
        n.text = text;
        notifyItemChanged(position);

        if (n.isVisible)
            setNotification(n);
    }

    /**
     * Get unique note id
     */
    private int getId()
    {
        boolean idOk = false;
        int id = 0;

        while (!idOk)
        {
            idOk = true;
            for (int i = 0; i < this.notes.size(); ++i)
            {
                if (this.notes.get(i).id == id)
                {
                    idOk = false;
                    ++id;
                    break;
                }
            }
        }

        return id;
    }

    private void setNotification(NotificationNote n)
    {
        Intent in = new Intent(this.context, NotificationService.class);

        in.putExtra(NotificationService.ID, n.id);
        in.putExtra(NotificationService.SHOW, n.isVisible);

        if (n.isVisible)
        {
            in.putExtra(NotificationService.TITLE, n.title);
            in.putExtra(NotificationService.TEXT, n.text);
        }

        this.context.startService(in);
    }
}

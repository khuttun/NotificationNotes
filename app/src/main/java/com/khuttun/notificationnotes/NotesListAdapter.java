package com.khuttun.notificationnotes;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
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
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private ArrayList<NotificationNote> notes;

    /**
     * ViewHolder holds the view showing one NotificationNote.
     * It also listens to UI actions in the view and forwards the actions to NotesController.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnLongClickListener, CompoundButton.OnCheckedChangeListener
    {
        public TextView titleView;
        public TextView textView;
        public SwitchCompat switchView;
        private NotesController notesController;

        public ViewHolder(View v, NotesController notesController)
        {
            super(v);

            this.titleView = (TextView) v.findViewById(R.id.note_title);
            this.textView = (TextView) v.findViewById(R.id.note_text);
            this.switchView = (SwitchCompat) v.findViewById(R.id.note_switch);
            this.notesController = notesController;

            v.setOnLongClickListener(this);
            this.switchView.setOnCheckedChangeListener(this);
        }

        @Override
        public boolean onLongClick(View v)
        {
            this.notesController.onNoteLongClick(getAdapterPosition());
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            this.notesController.onNoteCheckedChanged(getAdapterPosition(), isChecked);
        }
    }

    public NotesListAdapter(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder)
    {
        this.notificationManager = notificationManager;
        this.notificationBuilder = notificationBuilder;
        this.notes = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false);
        ViewHolder vh = new ViewHolder(v, this);
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
    public void onNoteLongClick(int position)
    {
        this.notificationManager.cancel(this.notes.get(position).id);
        this.notes.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onNoteCheckedChanged(int position, boolean isChecked)
    {
        NotificationNote n = this.notes.get(position);
        n.isVisible = isChecked;
        updateNotification(n);
    }

    public ArrayList<NotificationNote> getNotes()
    {
        return this.notes;
    }

    public void setNotes(ArrayList<NotificationNote> notes)
    {
        this.notes = notes;
        notifyDataSetChanged();
        for (int i = 0; i < this.notes.size(); ++i)
            updateNotification(this.notes.get(i));
    }

    public void addNote(String title, String text)
    {
        int id = getId();
        NotificationNote n = new NotificationNote(id, title, text, true);
        this.notes.add(n);
        notifyItemInserted(this.notes.size() - 1);
        updateNotification(n);
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

    private void updateNotification(NotificationNote n)
    {
        if (n.isVisible)
        {
            this.notificationBuilder.setContentTitle(n.title);
            this.notificationBuilder.setContentText(n.text);
            Notification notification = this.notificationBuilder.build();
            this.notificationManager.notify(n.id, notification);
        }
        else
            this.notificationManager.cancel(n.id);
    }
}

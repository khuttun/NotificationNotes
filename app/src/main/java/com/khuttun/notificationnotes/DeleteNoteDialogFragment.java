package com.khuttun.notificationnotes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class DeleteNoteDialogFragment extends DialogFragment
{
    public static DeleteNoteDialogFragment newInstance(int notePos, String noteTitle)
    {
        DeleteNoteDialogFragment frag = new DeleteNoteDialogFragment();
        Bundle args = new Bundle();
        args.putInt("notePos", notePos);
        args.putString("noteTitle", noteTitle);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final int notePos = getArguments().getInt("notePos");
        final String noteTitle = getArguments().getString("noteTitle");
        final String deletePrompt = getResources().getString(R.string.dialog_delete_note);
        final String dialogText = noteTitle.isEmpty() ?
            deletePrompt + "?" : deletePrompt + " \"" + noteTitle + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(dialogText)
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Log.d(Globals.TAG, "Confirm delete of note at position " + notePos);
                    ((MainActivity) getActivity()).deleteNote(notePos);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Log.d(Globals.TAG, "Cancel delete note at position " + notePos);
                }
            });

        return builder.create();
    }
}

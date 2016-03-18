package com.khuttun.notificationnotes;

/**
 * Interface for controller that handles NotificationNote UI actions.
 */
interface NotesController
{
    void onNoteClicked(int position);
    void onNoteCheckedChanged(int position, boolean isChecked);
}

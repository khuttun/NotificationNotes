package com.khuttun.notificationnotes;

class NotificationNote
{
    public int id;
    public String title;
    public String text;
    public boolean isVisible;

    public NotificationNote(int id, String title, String text, boolean isVisible)
    {
        this.id = id;
        this.title = title;
        this.text = text;
        this.isVisible = isVisible;
    }
}

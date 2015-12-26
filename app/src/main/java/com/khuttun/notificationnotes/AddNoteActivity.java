package com.khuttun.notificationnotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class AddNoteActivity extends AppCompatActivity
{
    private EditText titleInput;
    private EditText textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        titleInput = (EditText) findViewById(R.id.note_title_input);
        textInput = (EditText) findViewById(R.id.note_text_input);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.save_note:
                String title = titleInput.getText().toString();
                String text = textInput.getText().toString();
                Log.d(Globals.TAG, "Saving note. Title: " + title + ", Text: " + text);

                Intent data = new Intent();
                data.putExtra("title", title);
                data.putExtra("text", text);

                setResult(RESULT_OK, data);
                finish();

                break;
        }

        return true;
    }
}

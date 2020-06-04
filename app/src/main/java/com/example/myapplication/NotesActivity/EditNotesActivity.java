package com.example.myapplication.NotesActivity;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;

import androidx.appcompat.app.AppCompatActivity;

public class EditNotesActivity extends AppCompatActivity {
    private String selectedName;

    DatabaseHelper mDatabaseHelper;
    private int selectedID;
    private EditText text;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notes, menu);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        text = (EditText) findViewById(R.id.editText);
        mDatabaseHelper = new DatabaseHelper(this);
        Intent recievedIntent = getIntent();

        selectedID = recievedIntent.getIntExtra("id", -1);

        selectedName = recievedIntent.getStringExtra("name");

        text.setText(selectedName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                String editText = text.getText().toString();
                if (!editText.equals("")) {
                mDatabaseHelper.updateNote(editText,selectedID,selectedName);
                    Toast.makeText(EditNotesActivity.this, "Note Saved!", Toast.LENGTH_LONG).show();

                }
            case R.id.delete:
               mDatabaseHelper.deleteNote(selectedID, selectedName);;
               text.setText("");
                Toast.makeText(EditNotesActivity.this, "Note Deleted", Toast.LENGTH_LONG).show();

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

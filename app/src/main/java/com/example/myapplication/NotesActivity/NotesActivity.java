package com.example.myapplication.NotesActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;


import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NotesActivity extends AppCompatActivity {
    EditText fileName;
    EditText text;
    DatabaseHelper myDB;

    private String selectedName;
    private int selectedID;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notes, menu);
        menu.findItem(R.id.delete).setVisible(false);
        menu.findItem(R.id.delete).setEnabled(false);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        myDB = new DatabaseHelper(this);
        text = findViewById(R.id.editText);
    }
    private boolean externalStorageExists(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.i("State", "Yes, it's writable!");
            return true;
        }else{
            return false;
        }
    }
    //TODO create save storage options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                String newEntry = text.getText().toString();
                if(text.length() != 0) {
                    AddData(newEntry);
                    text.setText("");
                }else{
                Toast.makeText(NotesActivity.this, "You must put something in the text field!", Toast.LENGTH_LONG).show();
                }

            case R.id.delete:
                ;

        }
        return super.onOptionsItemSelected(item);
    }

    public void AddData(String newEntry){
        boolean insertData = myDB.addData(newEntry);

        if (insertData){
            Toast.makeText(NotesActivity.this, "Note Saved!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(NotesActivity.this, "I'm sorry, something went wrong :(", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent recievedIntent = getIntent();
        selectedID = recievedIntent.getIntExtra("id", -1 );
        selectedName = recievedIntent.getStringExtra("name");
        text.setText(selectedName);
    }
}

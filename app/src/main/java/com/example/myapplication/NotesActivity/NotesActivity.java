package com.example.myapplication.NotesActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.Database.NotesDBAdapter;
import com.example.myapplication.R;

public class NotesActivity extends AppCompatActivity {

    private NotesDBAdapter mDbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mDbAdapter = new NotesDBAdapter(this);
        mDbAdapter.open();
    }
}

package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.AudioRecording.AudioRecordingActivity;
import com.example.myapplication.CameraActivity.CameraActivity;
import com.example.myapplication.NotesActivity.EditNotesActivity;
import com.example.myapplication.NotesActivity.NotesActivity;
import com.example.myapplication.VideoRecording.VideoCamera;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
DatabaseHelper mDatabaseHelper;
ListView mListView;
ArrayList<String> listData= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mListView = (ListView) findViewById(R.id.listView);
        mDatabaseHelper = new DatabaseHelper(this);

        populateListView();
    }
    private void populateListView(){
        Cursor data = mDatabaseHelper.getData();

            while(data.moveToNext()) {
                listData.add(data.getString(1));
            }
                ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
                mListView.setAdapter(adapter);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = parent.getItemAtPosition(position).toString();

                        Cursor data = mDatabaseHelper.getItemID(name);
                        int itemID = -1;
                        while(data.moveToNext()){
                            itemID = data.getInt(0);
                        }
                        if(itemID > -1){
                            Intent editScreenIntent = new Intent(MainActivity.this, EditNotesActivity.class);
                            editScreenIntent.putExtra("id", itemID);
                            editScreenIntent.putExtra("name", name);
                            startActivity(editScreenIntent);
                        }
                    }
                });
            }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //if statements to call classes through actionbar
        if (id == R.id.camera) {
            Intent cameraIntent = new Intent (MainActivity.this, CameraActivity.class);
            startActivity(cameraIntent);
            return true;
        }

        if (id == R.id.new_note) {
                Intent notesIntent = new Intent(MainActivity.this, NotesActivity.class);
                startActivity(notesIntent);
                return false;
            }
        if (id == R.id.audioRecording) {
            Intent audioIntent = new Intent(MainActivity.this, AudioRecordingActivity.class);
            startActivity(audioIntent);
            return false;
        }

        if (id == R.id.videoViewer) {
            Intent videoIntent = new Intent(MainActivity.this, VideoCamera.class);
            startActivity(videoIntent);
            return false;
        }


        return super.onOptionsItemSelected(item);
    }

}

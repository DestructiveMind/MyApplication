package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import com.example.myapplication.AudioRecording.AudioRecordingActivity;
import com.example.myapplication.CameraActivity.CameraActivity;
import com.example.myapplication.NotesActivity.NotesActivity;
import com.example.myapplication.VideoRecording.VideoCamera;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

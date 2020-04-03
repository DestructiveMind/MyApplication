package com.example.myapplication.VideoRecording;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;

import androidx.appcompat.app.AppCompatActivity;

public class VideoCameraPreview extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recording);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

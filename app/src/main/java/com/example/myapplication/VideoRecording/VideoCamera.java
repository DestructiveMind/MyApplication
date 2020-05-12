package com.example.myapplication.VideoRecording;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.myapplication.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;


public class VideoCamera extends AppCompatActivity implements LifecycleOwner {
    private static final int REQUEST_VIDEO_CAPTURE = 101;
    VideoCamera mVideoCamera;
    ImageButton mRecordVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recording);
        VideoView videoView = (VideoView) findViewById(R.id.videoPreview);
        mRecordVideo = findViewById(R.id.recordVideo);
        if (!checkCameraHardware())
            mRecordVideo.setEnabled(false);

    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to: \n" + data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }

    }
}

package com.example.myapplication.VideoRecording;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;


public class VideoCamera extends AppCompatActivity implements LifecycleOwner {
    private static final int REQUEST_VIDEO_CAPTURE = 101;
    private Uri videoUri = null;
    VideoCamera mVideoCamera;
    ImageButton mRecordVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (videoIntent.resolveActivity(getPackageManager()) != null) ;
        {
            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
        }
//        setContentView(R.layout.activity_video_recording);
//        VideoView videoView = (VideoView) findViewById(R.id.videoPreview);
//        mRecordVideo = findViewById(R.id.recordVideo);
//        if (!checkCameraHardware())
//            mRecordVideo.setEnabled(false);

    }

    private boolean checkCameraHardware() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    public void captureVideo(View view) {
//        Intent videoIntent = new Intent (MediaStore.ACTION_VIDEO_CAPTURE);
//        if(videoIntent.resolveActivity(getPackageManager()) !=null);
//        {
//            startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            {
                videoUri = data.getData();
                Toast.makeText(this, "Video saved to: \n" + data.getData(), Toast.LENGTH_LONG).show();
                Intent mHomeIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(mHomeIntent, 0);
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            }

        }


    }


}

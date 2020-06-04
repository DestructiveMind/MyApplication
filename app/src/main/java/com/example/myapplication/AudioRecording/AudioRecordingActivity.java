package com.example.myapplication.AudioRecording;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Transcription.Transcriber;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AudioRecordingActivity extends Transcriber {
    Button mRecord;
    String AudioSavePathInDevice = null;
    public static final int RequestPermissionCode = 1000;
    private VisualizerView visualizerView;
    MediaPlayer mPlayer;
    Random random;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    private Boolean currentlyRecording;
    private MediaRecorder recorder = new MediaRecorder();
    private Handler handler = new Handler();

    //Runnable updater
    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 1);
            int maxAmplitude = recorder.getMaxAmplitude();
            if (maxAmplitude != 0) {
                visualizerView.addAmplitude(maxAmplitude);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        visualizerView = findViewById(R.id.visualizer);
        returnedText = findViewById(R.id.transcribeNote);
        mRecord = findViewById(R.id.recordAudio);
        mRecord.setTag(1);
        mRecord.setText("Record");
        Log.d("Audio Recorder", mRecord.toString());
        random = new Random();
        currentlyRecording = false;

                mRecord.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void onClick(View v) {
                final int status = (Integer) v.getTag();
                Log.d("Audio Status", "Permissions status: " + Boolean.toString(checkPermission()));

                //check if the user has provided permission
                if (checkPermission()) {
                    //check if the app is currently recording
                    if (!currentlyRecording) {
                        //AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath()
                        //        + "." + "/" + CreateRandomAudioFileName(5) + "AudioRecording.3gp";

                        //create a new media recorder
                        MediaRecorderReady();

                        // start listening
                        speech.startListening(recognizerIntent);

                        //try to prepare and start the recorder
                        try {
                            recorder.prepare();
                            recorder.start();

                        } catch (IllegalStateException | IOException e) {
                            Log.d("Start Recording Error", e.toString());
                        }


                        Toast.makeText(AudioRecordingActivity.this, "Recording Started", Toast.LENGTH_LONG).show();
                        if (status == 1) {
                            mRecord.setText("Stop Recording");
                            mRecord.setBackgroundColor(Color.RED);
                            mRecord.setTag(0);
                        }

                        //modify the currently
                        currentlyRecording = true;
                    } else {
                        //stop listening
                        speech.stopListening();

                        //Uri audioUri = recognizerIntent.getData();
                        //Log.d(LOG_TAG, audioUri.toString());

                        //try to stop the recording
                        try {

                            recorder.stop();
                        } catch (IllegalStateException e) {
                            Log.d("End Recording Error", e.toString());
                        }

                        //notify the user recording has stopped
                        Toast.makeText(AudioRecordingActivity.this, "Recording Stopped", Toast.LENGTH_LONG).show();

                        //convert back the button text and color
                        if (status == 0) {
                            mRecord.setText("Start Recording");
                            mRecord.setBackgroundColor(Color.GRAY);
                            mRecord.setTag(1);
                        }

                        //modify the currently recording variable
                        currentlyRecording = false;

                        //display the stored file names
                        String[] files = getApplicationContext().fileList();

                        StringBuilder file_names = new StringBuilder();

                        for (String file : files) {
                            file_names.append(file).append(" ");
                        }

                        returnedText.setText(file_names.toString());
                    }
                } else {
                    //if the user has not provided permission
                    requestPermission();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void MediaRecorderReady() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        
        //get a file object
        Context context = getApplicationContext();
        File file = new File(context.getFilesDir(), "temp");

        //set it as the output file
        recorder.setOutputFile(file);
    }

    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        //request audio recording permissions
        ActivityCompat.requestPermissions(AudioRecordingActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, INTERNET}, RequestPermissionCode);

        Log.d("Audio Record:", "permission requested");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    Log.d("Storage Permission", "" + Boolean.toString(StoragePermission));
                    if (RecordPermission) {
                        Toast.makeText(AudioRecordingActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AudioRecordingActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        //check if the audio recording permission was provided
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);

        //return
        return result == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updater);
        handler.removeCallbacks(updater);
        if (currentlyRecording) {
            recorder.stop();
            recorder.reset();
            recorder.release();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        handler.post(updater);
    }

}
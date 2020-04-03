package com.example.myapplication.AudioRecording;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class TranscriptionActivity extends AppCompatActivity {
  private TextView tNote;
  private final int REQ_CODE=101;

    public void getSpeechInput(View view, Context context) {

        tNote = (TextView) findViewById(R.id.transcribeNote);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(context.getPackageManager()) != null) {
           try {
               startActivityForResult(intent, REQ_CODE);
           } catch (ActivityNotFoundException a){
               Toast.makeText(getApplicationContext(), "Device Not Supported", Toast.LENGTH_SHORT).show();
           }
        } else {
            Toast.makeText(this, "Speech Input Not Supported", Toast.LENGTH_LONG).show();
        }
    }
        @Override
                protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case REQ_CODE:
                    if(requestCode == RESULT_OK && null != data) {
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        tNote.setText(result.get(0));
                    }
                    break;
            }
        }
    }


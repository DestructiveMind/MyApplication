package com.example.myapplication.AudioRecording;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Transcription.Transcriber;
import com.google.api.client.util.IOUtils;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechGrpc;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.okhttp.OkHttpChannelProvider;

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



    //////GOOGLE API VARIABLES/////
    private static final String PREFS = "SpeechService";
    private static final String PREF_ACCESS_TOKEN_VALUE = "access_token_value";
    private static final String PREF_ACCESS_TOKEN_EXPIRATION_TIME = "2000";

    /** We reuse an access token if its expiration time is longer than this. */
    private static final int ACCESS_TOKEN_EXPIRATION_TOLERANCE = 30 * 60 * 1000; // thirty minutes
    /** We refresh the current access token before it expires. */
    private static final int ACCESS_TOKEN_FETCH_MARGIN = 60 * 1000; // one minute

    public static final List<String> SCOPE =
            Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");
    private static final String HOSTNAME = "speech.googleapis.com";
    private static final int PORT = 443;

    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private volatile AccessTokenTask mAccessTokenTask;
    private SpeechGrpc.SpeechStub mApi;
        private static Handler mHandler;


    //GOOGLE LISTENER
    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param text    The text.
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String text, boolean isFinal);

    }

    private void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }
    private class SpeechBinder extends Binder {

        AudioRecordingActivity getService() {
            return AudioRecordingActivity.this;
        }

    }


    private final Runnable mFetchAccessTokenRunnable = new Runnable() {
        @Override
        public void run() {
            fetchAccessToken();
        }
    };
    private class AccessTokenTask extends AsyncTask<Void, Void, AccessToken> {

        @Override
        protected AccessToken doInBackground(Void... voids) {
            final SharedPreferences prefs =
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null);
            long expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1);

            // Check if the current token is still valid for a while
            if (tokenValue != null && expirationTime > 0) {
                if (expirationTime
                        > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE) {
                    return new AccessToken(tokenValue, new Date(expirationTime));
                }
            }
            final InputStream stream = getResources().openRawResource(R.raw.credential);
            try {
                final GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                        .createScoped(SCOPE);
                final AccessToken token = credentials.refreshAccessToken();
                prefs.edit()
                        .putString(PREF_ACCESS_TOKEN_VALUE, token.getTokenValue())
                        .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME,
                                token.getExpirationTime().getTime())
                        .apply();
                return token;
            } catch (IOException ignored) {

            }
            return null;
        }
        @Override
        protected void onPostExecute(AccessToken accessToken) {
            mAccessTokenTask = null;
            final ManagedChannel channel = new OkHttpChannelProvider()
                    .builderForAddress(HOSTNAME, PORT)
                    .nameResolverFactory(new DnsNameResolverProvider())
                    .intercept(new GoogleCredentialsInterceptor(new GoogleCredentials(accessToken)
                            .createScoped(SCOPE)))
                    .build();

            mApi = SpeechGrpc.newStub(channel);

            // Schedule access token refresh before it expires
            if (mHandler != null) {
                mHandler.postDelayed(mFetchAccessTokenRunnable,
                        Math.max(accessToken.getExpirationTime().getTime()
                                - System.currentTimeMillis()
                                - ACCESS_TOKEN_FETCH_MARGIN, ACCESS_TOKEN_EXPIRATION_TOLERANCE));
            }
        }
    }

        ////////////
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
        fetchAccessToken();


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

                        ////////////////////////////

                        // Instantiates a client
                        new Thread(new Runnable() {
                            public void run() {



                        try (SpeechClient speechClient = SpeechClient.create()) {
                            Context context = getApplicationContext();
                            FileInputStream fis = context.openFileInput("temp");
                            byte[] audioContents = IOUtils.deserialize(fis);


                            ByteString audioBytes = ByteString.copyFrom(audioContents);

                            // Builds the sync recognize request
                            RecognitionConfig config =
                                    RecognitionConfig.newBuilder()
                                            .setEncoding(AudioEncoding.LINEAR16)
                                            .setSampleRateHertz(16000)
                                            .setLanguageCode("en-US")
                                            .build();
                            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

                            // Performs speech recognition on the audio file
                            RecognizeResponse response = speechClient.recognize(config, audio);
                            List<SpeechRecognitionResult> results = response.getResultsList();

                            for (SpeechRecognitionResult result : results) {
                                // There can be several alternative transcripts for a given chunk of speech. Just use the
                                // first (most likely) one here.
                                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                                System.out.printf("Transcription: %s%n", alternative.getTranscript());
                                returnedText.setText(alternative.getTranscript());
                            }
                        } catch (java.io.IOException e) {
                            Log.d("Speech Translator", e.toString());
                        }
                    }
                }).start();
                        /////////////////
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
       mHandler.removeCallbacks(mFetchAccessTokenRunnable);
       mHandler = null;
        // handler.removeCallbacks(updater);
      //  handler.removeCallbacks(updater);
        if (currentlyRecording && mApi != null) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            final ManagedChannel channel = (ManagedChannel) mApi.getChannel();
            if (channel != null && !channel.isShutdown()){
                try {
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException d) {
                    Log.d("Audio Recording", "Error shutting down the gRPC channel.", d);
                }
            }
            mApi = null;
        }
    }
    private String getDefaultLanguageCode() {
        final Locale locale = Locale.getDefault();
        final StringBuilder language = new StringBuilder(locale.getLanguage());
        final String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            language.append("-");
            language.append(country);
        }
        return language.toString();
    }
    @Nullable
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
       // handler.post(updater);
    }

    /**
     * Authenticates the gRPC channel using the specified {@link GoogleCredentials}.
     */
    private static class GoogleCredentialsInterceptor implements ClientInterceptor {

        private final Credentials mCredentials;

        private Metadata mCached;

        private Map<String, List<String>> mLastMetadata;

        GoogleCredentialsInterceptor(Credentials credentials) {
            mCredentials = credentials;
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                final MethodDescriptor<ReqT, RespT> method, CallOptions callOptions,
                final Channel next) {
            return new ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(
                    next.newCall(method, callOptions)) {
                @Override
                protected void checkedStart(Listener<RespT> responseListener, Metadata headers)
                        throws StatusException {
                    Metadata cachedSaved;
                    URI uri = serviceUri(next, method);
                    synchronized (this) {
                        Map<String, List<String>> latestMetadata = getRequestMetadata(uri);
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata;
                            mCached = toHeaders(mLastMetadata);
                        }
                        cachedSaved = mCached;
                    }
                    headers.merge(cachedSaved);
                    delegate().start(responseListener, headers);
                }
            };
        }

        /**
         * Generate a JWT-specific service URI. The URI is simply an identifier with enough
         * information for a service to know that the JWT was intended for it. The URI will
         * commonly be verified with a simple string equality check.
         */
        private URI serviceUri(Channel channel, MethodDescriptor<?, ?> method)
                throws StatusException {
            String authority = channel.authority();
            if (authority == null) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Channel has no authority")
                        .asException();
            }
            // Always use HTTPS, by definition.
            final String scheme = "https";
            final int defaultPort = 443;
            String path = "/" + MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            URI uri;
            try {
                uri = new URI(scheme, authority, path, null, null);
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI for auth")
                        .withCause(e).asException();
            }
            // The default port must not be present. Alternative ports should be present.
            if (uri.getPort() == defaultPort) {
                uri = removePort(uri);
            }
            return uri;
        }

        private URI removePort(URI uri) throws StatusException {
            try {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), -1 /* port */,
                        uri.getPath(), uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Unable to construct service URI after removing port")
                        .withCause(e).asException();
            }
        }

        private Map<String, List<String>> getRequestMetadata(URI uri) throws StatusException {
            try {
                return mCredentials.getRequestMetadata(uri);
            } catch (IOException e) {
                throw Status.UNAUTHENTICATED.withCause(e).asException();
            }
        }

        private static Metadata toHeaders(Map<String, List<String>> metadata) {
            Metadata headers = new Metadata();
            if (metadata != null) {
                for (String key : metadata.keySet()) {
                    Metadata.Key<String> headerKey = Metadata.Key.of(
                            key, Metadata.ASCII_STRING_MARSHALLER);
                    for (String value : metadata.get(key)) {
                        headers.put(headerKey, value);
                    }
                }
            }
            return headers;
        }

    }

}
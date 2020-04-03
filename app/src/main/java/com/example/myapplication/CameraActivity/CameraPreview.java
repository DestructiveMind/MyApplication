package com.example.myapplication.CameraActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
import java.lang.reflect.Method;

import androidx.annotation.RequiresApi;

import static android.content.ContentValues.TAG;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {

        super(context);
        mCamera = camera;
        //SurfaceHolder.callback to notify us when the underlying surface is created or destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);
        //Required for Android 3.0<
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Creates the preview surface for CameraActivity
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //tells the camera where to draw preview
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview " + e.getMessage());
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 0) {
            mCamera.setDisplayOrientation(90);
        } else if (newConfig.orientation == 1) {
            mCamera.setDisplayOrientation(90);
        } else if (newConfig.orientation == 2) {
            mCamera.setDisplayOrientation(180);}

    }

    /**
     * Handles changes to the preview surface for CameraActivity
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            //surface doesn't exist
            return;
        }
        //stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
// TODO: 1/30/2020  fix orientation so that the camera shows correctly on initialization
        //set preview size and reformatting changes here
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * Releases the preview surface for CameraActivity
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Released from activity not from preview Class
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}

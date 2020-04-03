package com.example.myapplication.AudioRecording;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.Locale;

class VisualizerView extends View {
    private static final int MAX_AMPLITUDE = 32767;

    private float[] amplitudes;
    private float[] vectors;
    private int insertIdx = 0;
    private Paint pointPaint;
    private Paint linePaint;
    private int width;
    private int height;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(5);
        pointPaint = new Paint();
        pointPaint.setColor(Color.DKGRAY);
        pointPaint.setStrokeWidth(1);
    }

    @Override
    protected void onSizeChanged(int width, int h, int oldw, int oldh) {
        this.width = width;
        height = h;
        amplitudes = new float[this.width * 2]; // xy for each point across the width
        vectors = new float[this.width * 4]; // xxyy for each line across the width
    }

    /**
     * modifies draw arrays. cycles back to zero when amplitude samples reach max screen size
     */
    public void addAmplitude(int amplitude) {
        invalidate();
        float scaledHeight = ((float) amplitude / MAX_AMPLITUDE) * (height - 1);
        int ampIdx = insertIdx * 2;
        amplitudes[ampIdx++] = insertIdx;   // x
        amplitudes[ampIdx] = scaledHeight;  // y
        int vectorIdx = insertIdx * 4;
        vectors[vectorIdx++] = insertIdx;   // x0
        vectors[vectorIdx++] = 0;           // y0
        vectors[vectorIdx++] = insertIdx;   // x1
        vectors[vectorIdx] = scaledHeight;  // y1
        // insert index must be shorter than screen width
        insertIdx = ++insertIdx >= width ? 0 : insertIdx;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLines(vectors, linePaint);
        canvas.drawPoints(amplitudes, pointPaint);
    }

}
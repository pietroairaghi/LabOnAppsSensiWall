package project.labonappssensiwall;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawActivity extends WearableActivity {

    private LinearLayout mLayout;
    private View mShape;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorListener;

    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 8.0f;
    private static final int ROTATION_WAIT_TIME_MS = 500;

    private static final List<String> shapes = Arrays.asList("circle", "square", "triangle");
    private int currentShape = 0;

    private long mShakeTime = 0;
    private long mRotationTime = 0;

    private float[] lastCoords = {0f,0f,0f};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mLayout = findViewById(R.id.container);
        mShape  = findViewById(R.id.shapeButton);

        // Enables Always-on
        setAmbientEnabled();

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    detectShake(event);
                }
                else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    detectRotation(event);
                }
            }
        };

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                sendDraw();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateDisplay() {
        if(isAmbient()){
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.black,getTheme()));
        }else{
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark,getTheme()));
        }
    }

    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            lastCoords = event.values;

            // gForce will be close to 1 when there is no movement
            double gForce = Math.sqrt(gX*gX + gY*gY + gZ*gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if(gForce > SHAKE_THRESHOLD) {
                mLayout.setBackgroundColor(Color.rgb(0, 100, 0));
            }
            else {
                mLayout.setBackgroundColor(Color.BLACK);
            }
        }
    }

    private void detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            // Change background color if rate of rotation around any
            // axis and in any direction exceeds threshold;
            // otherwise, reset the color
            if(Math.abs(event.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(event.values[2]) > ROTATION_THRESHOLD) {

                //change current shape
                currentShape = (currentShape+1)%3;
                String shapeName = shapes.get(currentShape) + "_shape";

                Context context = mShape.getContext();
                int id = context.getResources().getIdentifier(shapeName, "drawable", context.getPackageName());
                mShape.setBackground(getDrawable(id));
            }
        }
    }

    public void sendDraw() {
        Intent intentWear = new Intent(this,WearService.class);
        intentWear.setAction(WearService.ACTION_SEND.START_DRAW.name());
        intentWear.putExtra("gX",lastCoords[0]);
        intentWear.putExtra("gY",lastCoords[1]);
        intentWear.putExtra("gZ",lastCoords[2]);
        intentWear.putExtra("shape",shapes.get(currentShape));
        intentWear.putExtra(WearService.PATH,"setDraw");
        startService(intentWear);
    }
}

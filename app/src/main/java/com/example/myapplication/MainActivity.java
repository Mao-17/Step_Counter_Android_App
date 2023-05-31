package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorEvent mLastAccelerometerEvent;

    private TextView mStepCountTextView;
    private TextView mDirectionTextView;

    private TextView lift;
    private float[] prevVelocity = new float[3];
    private float[] prevDisplacement = new float[3];
    private float prevTimestamp = 0;
    private float[] accelValues = new float[3];
    private List<PointF> trajectoryPoints;
    private float[] prevAccelValues = new float[3];
    private float[] prevmagnetometer = new float[3];
    private int steps = 0;
    private TrajectoryView trajectoryView;

    private float prevAccelerationValue;
    private float prevAccelerationMagnitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trajectoryPoints = new ArrayList<>();

        // Initialize the SensorManager and the sensors we will use
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Initialize UI elements
        mStepCountTextView = findViewById(R.id.step_count_textview);
        mDirectionTextView = findViewById(R.id.direction_textview);
        lift = findViewById(R.id.lift_textview);

        trajectoryView = findViewById(R.id.trajectory_view);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Register sensor listeners
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        trajectoryPoints.clear();
        startUIUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister sensor listeners
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not implemented
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mLastAccelerometerEvent = event;
            handleAccelerometerEvent(event);
            PointF lastPoint = trajectoryView.getLastPoint();
            float x = lastPoint != null ? lastPoint.x : 0;
            float y = lastPoint != null ? lastPoint.y : 0;
            trajectoryView.addPoint(x + event.values[0], y - event.values[1]);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            handleMagnetometerEvent(event);
        }
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (trajectoryView != null) {
                    PointF lastPoint = trajectoryView.getLastPoint();
                    if (lastPoint != null) {
                        trajectoryPoints.add(lastPoint);
                        trajectoryView.addPoint(lastPoint.x, lastPoint.y);
                    }
                }
            }
        });
    }



    private void startUIUpdateTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        }, 0, 1000); // Update UI every second
    }
    private void handleAccelerometerEvent(SensorEvent event) {
        float[] accelValues = event.values.clone();
        float timestamp = event.timestamp;

        // Compute time interval since last sensor event
        float dt = (timestamp - prevTimestamp) / 1e9f; // convert nanoseconds to seconds
        prevTimestamp = timestamp;

        // Compute velocity and displacement by integrating acceleration
        float[] velocity = new float[3];
        float[] displacement = new float[3];
        for (int i = 0; i < 3; i++) {
            velocity[i] = prevVelocity[i] + (accelValues[i] + prevAccelValues[i]) / 2 * dt;
            displacement[i] = prevDisplacement[i] + velocity[i] * dt;
        }

        // Compute magnitude of acceleration and displacement vectors
        float accelerationMagnitude = (float) Math.sqrt(accelValues[0] * accelValues[0] + accelValues[1] * accelValues[1] + accelValues[2] * accelValues[2]);
        float displacementMagnitude = (float) Math.sqrt(displacement[0] * displacement[0] + displacement[1] * displacement[1] + displacement[2] * displacement[2]);

        // Compute stride length using the user's height
        float strideLength = 1.73f * 0.413f;

        // Update previous velocity and displacement
        prevVelocity = velocity;
        prevDisplacement = displacement;
        prevAccelValues = accelValues.clone();

        if (prevAccelerationValue > 0 && accelerationMagnitude < prevAccelerationMagnitude) {
            // Peak detected
            if (accelerationMagnitude > 10) { // Adjust threshold to your needs
                steps++;
                mStepCountTextView.setText(String.valueOf(steps));
            }
        }

        prevAccelerationValue = accelerationMagnitude;
        prevAccelerationMagnitude = accelerationMagnitude;

        lift.setText(getMovementType(accelValues));

    }

    private void handleMagnetometerEvent(SensorEvent event) {
        float[] magneticValues = event.values;
        float x = magneticValues[0];
        float y = magneticValues[1];
        float z = magneticValues[2];

        // Compute the magnitude of the magnetic field vector
        float magneticMagnitude = (float) Math.sqrt(x * x + y * y + z * z);
        float orientation = (float) Math.atan2(-x, y);
        float degrees = (float) Math.toDegrees(orientation);
        if (degrees < 0) {
            degrees += 360;
        }

        prevmagnetometer = magneticValues.clone();

        // Calculate the direction based on the orientation in degrees
        String direction;
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "North";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "North East";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "East";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "South East";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "South";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "South West";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "West";
        } else {
            direction = "North West";
        }

        // Update the direction text view
        mDirectionTextView.setText("Direction: " + direction);
    }
    public String getMovementType(float[] accelerometerData) {
        // Calculate the magnitude of the accelerometer data vector
        float magnitude = (float) Math.sqrt(accelerometerData[0] * accelerometerData[0] +
                accelerometerData[1] * accelerometerData[1] +
                accelerometerData[2] * accelerometerData[2]);
        // Determine the movement type based on the magnitude of the accelerometer data
        if (magnitude > 15.0f) { // threshold for taking stairs
            return "Taking stairs";
        } else if (magnitude > 12.0f && magnitude < 15.0f) { // threshold for taking lift
            return "Taking lift";
        } else {
            return "Neither stairs nor lift";
        }
    }

}
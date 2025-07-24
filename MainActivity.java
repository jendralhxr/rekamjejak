package com.example.sensordatalogger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accel, gyro;
    private FileWriter fileWriter;
    private boolean recording = false;

    private EditText filenameInput;
    private Button startButton, stopButton;

    private final int REQUEST_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        filenameInput = findViewById(R.id.filename_input);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        checkPermissions();

        startButton.setOnClickListener(v -> startRecording());
        stopButton.setOnClickListener(v -> stopRecording());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            }
        }
    }

    private void startRecording() {
        if (recording) return;

        String baseName = filenameInput.getText().toString().trim();
        if (baseName.isEmpty()) {
            Toast.makeText(this, "Enter filename", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fullFilename = baseName + "_" + timestamp + ".csv";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fullFilename);

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write("timestamp,sensor,x,y,z\n");
            recording = true;
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to open file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (!recording) return;

        recording = false;
        sensorManager.unregisterListener(this);
        try {
            fileWriter.close();
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to close file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!recording || fileWriter == null) return;

        long timestamp = System.currentTimeMillis();
        String sensorType = event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ? "ACCEL" : "GYRO";
        String line = timestamp + "," + sensorType + "," +
                event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n";

        try {
            fileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

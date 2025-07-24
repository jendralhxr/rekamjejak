package com.example.sensordatalogger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorService extends Service implements SensorEventListener {

    private static final String CHANNEL_ID = "SensorLoggerChannel";
    private SensorManager sensorManager;
    private Sensor accel, gyro;
    private FileWriter fileWriter;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        String baseName = intent.getStringExtra("filename");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fullFilename = baseName + "_" + timestamp + ".csv";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fullFilename);
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write("timestamp,sensor,x,y,z\n");
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
            return START_NOT_STICKY;
        }

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sensor Logger")
                .setContentText("Recording sensor data...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Sensor Logger", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (fileWriter == null) return;
        long ts = System.currentTimeMillis();
        String type = (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? "ACCEL" : "GYRO";
        String line = ts + "," + type + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n";
        try {
            fileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}

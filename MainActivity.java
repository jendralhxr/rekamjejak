package com.example.sensordatalogger;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText filenameInput;
    private Button startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filenameInput = findViewById(R.id.filename_input);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        startButton.setOnClickListener(v -> {
            String baseName = filenameInput.getText().toString().trim();
            if (baseName.isEmpty()) {
                Toast.makeText(this, "Enter filename", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, SensorService.class);
            intent.putExtra("filename", baseName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            Toast.makeText(this, "Logging started", Toast.LENGTH_SHORT).show();
        });

        stopButton.setOnClickListener(v -> {
            stopService(new Intent(this, SensorService.class));
            Toast.makeText(this, "Logging stopped", Toast.LENGTH_SHORT).show();
        });
    }
}

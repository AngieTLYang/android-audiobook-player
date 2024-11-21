package com.example.audiobookplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SettingsActivity extends AppCompatActivity {

    private Spinner speedSpinner;
    private Button colorPickerButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("start setting activity", "start setting activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        // settingsLayout = findViewById(R.id.activity_setting);
        speedSpinner = findViewById(R.id.playback_speeds);
        colorPickerButton = findViewById(R.id.color_picker_button);

        // Set up playback speed options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.playback_speeds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(adapter);

        // Handle spinner selection
        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpeed = parent.getItemAtPosition(position).toString();
                Log.d("speed spinner interaction", selectedSpeed + "was selected");
                // Convert the selected speed to a float (for MediaPlayer)
                float playbackSpeed = Float.parseFloat(selectedSpeed);

                // Save it in SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("selectedSpeed", playbackSpeed);
                editor.apply();
                // Handle speed changes, e.g., send it back to the main activity
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        // Set background color
        colorPickerButton.setOnClickListener(v -> {
            // Define a list of colors (same as the ones in colors.xml)
            String[] colorNames = {"Red", "Blue", "Green", "Yellow", "Purple", "Black"};
            int[] colors = {
                    Color.parseColor("#F44336"),  // Red
                    Color.parseColor("#2196F3"),  // Blue
                    Color.parseColor("#4CAF50"),  // Green
                    Color.parseColor("#FFEB3B"),  // Yellow
                    Color.parseColor("#9C27B0"),  // Purple
                    Color.parseColor("#000000")   // Black
            };

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Pick a Color");
            builder.setItems(colorNames, (dialog, which) -> {
                // Get the selected color and apply it
                int selectedColor = colors[which];
                ConstraintLayout cardView = findViewById(R.id.cardView);
                cardView.setBackgroundColor(selectedColor);  // Apply the selected color
                Log.d("color picker interaction", selectedColor + "was selected");
                SharedPreferences sharedPref = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("selectedColor", selectedColor);
                editor.apply();
            });

            // Show the dialog
            builder.create().show();
        });
    }
}

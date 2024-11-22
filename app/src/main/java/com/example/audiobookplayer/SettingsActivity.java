package com.example.audiobookplayer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
                R.array.playback_speeds, R.layout.item_spinner); // Use custom layout
        adapter.setDropDownViewResource(R.layout.item_spinner); // Use custom layout for dropdown
        speedSpinner.setAdapter(adapter);

        // Handle spinner selection
        speedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSpeed = parent.getItemAtPosition(position).toString();
                Log.d("speed spinner interaction", selectedSpeed + "was selected");

                try {
                    // Remove the "x" from the string, e.g., "0.5x" -> "0.5"
                    String numericSpeed = selectedSpeed.replace("x", "").trim();

                    // Convert the cleaned string to a float
                    float playbackSpeed = Float.parseFloat(numericSpeed);

                    // Save the speed in SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences("settings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putFloat("selectedSpeed", playbackSpeed);
                    editor.apply();

                } catch (NumberFormatException e) {
                    Log.e("speed spinner error", "Invalid playback speed: " + selectedSpeed, e);
                }
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

            // Use a custom adapter to set the layout for the color options
            builder.setAdapter(new ArrayAdapter<String>(SettingsActivity.this, R.layout.item_color_picker, colorNames),
                    (dialog, which) -> {
                        try {
                            int selectedColor = colors[which];
                            ConstraintLayout cardView = findViewById(R.id.cardView);
                            if (cardView != null) {
                                cardView.setBackgroundColor(selectedColor);
                            } else {
                                Log.e("SettingsActivity", "CardView is null");
                            }

                            SharedPreferences sharedPref = getSharedPreferences("settings", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("selectedColor", selectedColor);
                            editor.apply();
                        } catch (Exception e) {
                            Log.e("SettingsActivity", "Error in color picker", e);
                        }
                    });
            // Show the dialog
            builder.create().show();
        });
    }
}

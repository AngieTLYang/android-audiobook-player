package com.example.audiobookplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class AudiobookListActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private RecyclerView recyclerView;
    private AudiobookPlayerAdapter adapter;
    private List<Audiobook> audiobookList = new ArrayList<>();
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1;
    private Button settingButton;
    ArrayList<Audiobook> arrayList = new ArrayList<>(audiobookList);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_audiobook_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mediaPlayer = new MediaPlayer();
        handler = new Handler();
        // Initialize the list of audiobooks
        String path = "/storage/emulated/0/Music/";
        File musicDirectory = new File(path);
        audiobookList = new ArrayList<>();
        // Check if the directory exists
        if (musicDirectory.exists() && musicDirectory.isDirectory()) {
            // Get the list of files in the directory
            File[] files = musicDirectory.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Only process .mp3 files
                    if (file.isFile()) {
                        // Extract author and title from the filename
                        String fileName = file.getName();
                        String[] parts = fileName.split(" - ");

                        if (parts.length == 2) {
                            String author = parts[0].trim(); // Author part before " - "
                            String title = parts[1].trim(); // Title part after " - "

                            // Create an Audiobook object and add it to the list
                            Audiobook audiobook = new Audiobook(title, author, file.getPath(), 0L);
                            audiobookList.add(audiobook);
                        }
                    }
                }
            }
        }

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter and handle audiobook clicks
        adapter = new AudiobookPlayerAdapter(audiobookList, new AudiobookPlayerAdapter.OnAudiobookClickListener() {
            @Override
            public void onAudiobookClick(Audiobook audiobook) {
                int position = audiobookList.indexOf(audiobook);
                // Pass the audiobook details to AudiobookPlayerActivity
                Log.d("AudiobookPlayer", "Title: " + audiobook.getTitle() + ", Author: " + audiobook.getAuthor() + ", FilePath: " + audiobook.getFilePath());
                Intent intent = new Intent(AudiobookListActivity.this, AudiobookPlayerActivity.class);
                intent.putParcelableArrayListExtra("audiobookList", new ArrayList<>(audiobookList));
                intent.putExtra("title", audiobook.getTitle());
                intent.putExtra("author", audiobook.getAuthor());
                intent.putExtra("filePath", audiobook.getFilePath());
                intent.putExtra("currentIndex", position);  // Pass the position of the audiobook
                // Ensure these values are not null
                if (audiobook.getTitle() == null || audiobook.getAuthor() == null || audiobook.getFilePath() == null) {
                    throw new IllegalArgumentException("Audiobook data is missing.");
                }
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
        checkAndRequestPermissions();

        // Initialize the button
        settingButton = findViewById(R.id.setting_button);

        // Set the click listener for the setting button
        settingButton.setOnClickListener(v -> {
            Log.d("button click", "setOnClickListener");
            Toast.makeText(AudiobookListActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
            // Start the SettingsActivity when the button is clicked
            Intent intent = new Intent(AudiobookListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Manifest.permission.READ_MEDIA_AUDIO", "!= PackageManager.PERMISSION_GRANTED");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                Log.d("shouldShowRequestPermissionRationale", "Manifest.permission.READ_MEDIA_AUDIO");
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(AudiobookListActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
            }
        } else {
            // Permission already granted
            // No need to load the audiobook here; it's handled by item click
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, do nothing as the audiobooks will be loaded on item click
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show(); // Permission denied, show a toast
            }
        }
    }
/*
    private void onAudiobookItemClick(Audiobook audiobook) {
        // Load the audiobook when the item is clicked
        loadAudiobook(audiobook.getFilePath());
    }
*/
    private void loadAudiobook(String filePath) {
        // Load the audiobook using the file path
        adapter.load(filePath, 1); // Assuming this method exists in your AudiobookPlayerAdapter class
    }

}
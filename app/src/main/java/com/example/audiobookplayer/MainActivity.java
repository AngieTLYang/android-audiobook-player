package com.example.audiobookplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.SeekBar;
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

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Audiobook selectedAudiobook;
    private Handler handler;
    private Runnable updateSeekbarRunnable;
    private RecyclerView recyclerView;
    private AudiobookAdapter adapter;
    private List<Audiobook> audiobookList;
    private AudiobookPlayer audiobookPlayer;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
                            Audiobook audiobook = new Audiobook(author, title, file.getPath(), 0L);
                            audiobookList.add(audiobook);
                        }
                    }
                }
            }
        }

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and set it to the RecyclerView
        AudiobookAdapter adapter = new AudiobookAdapter(audiobookList, new AudiobookAdapter.OnAudiobookClickListener() {
            @Override
            public void onAudiobookClick(Audiobook audiobook) {
                // Set the selected audiobook
                selectedAudiobook = audiobook;
                loadAudiobook(audiobook.getFilePath());  // Load the selected audiobook
                mediaPlayer.start();  // Start playing the audiobook
            }
        });

        recyclerView.setAdapter(adapter);

        audiobookPlayer = new AudiobookPlayer();
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Manifest.permission.READ_MEDIA_AUDIO", "!= PackageManager.PERMISSION_GRANTED");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                Log.d("shouldShowRequestPermissionRationale", "Manifest.permission.READ_MEDIA_AUDIO");
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
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

    private void onAudiobookItemClick(Audiobook audiobook) {
        // Load the audiobook when the item is clicked
        loadAudiobook(audiobook.getFilePath());
    }

    private void loadAudiobook(String filePath) {
        // Load the audiobook using the file path
        audiobookPlayer.load(filePath, 1); // Assuming this method exists in your AudiobookPlayer class
    }
}
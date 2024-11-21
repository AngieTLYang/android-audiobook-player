package com.example.audiobookplayer;

import android.content.Intent;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class AudiobookPlayerActivity extends AppCompatActivity {
    private long savedPosition = 0; // Save the position when paused or stopped
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private BookmarkManager bookmarkManager;
    private ArrayList<Audiobook> audiobookList; // List of audiobooks
    private SeekBar seekBar;
    private TextView tvTitle, tvAuthor;
    private ImageButton btnPlay, btnPause, btnStop, btnPrev, btnNext;
    String title, author, filePath;
    private int currentIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_player);

        // Initialize UI elements
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);
        Intent intent = getIntent();
        audiobookList = getIntent().getParcelableArrayListExtra("audiobookList");
        if (audiobookList == null || audiobookList.isEmpty()) {
            Toast.makeText(this, "No audiobooks available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();
        handler = new Handler();
        bookmarkManager = new BookmarkManager(this);
        bookmarkManager.open();

        if (intent != null) {
            currentIndex = intent.getIntExtra("currentIndex", -1); // Default to -1
            title = intent.getStringExtra("title");
            author = intent.getStringExtra("author");
            filePath = intent.getStringExtra("filePath");
        }
        if (title == null || author == null || filePath == null) {
            Log.e("AudiobookPlayerActivity", "Received null audiobook data.");
            Toast.makeText(this, "Invalid audiobook data. Unable to play.", Toast.LENGTH_SHORT).show();
            finish(); // End activity if data is invalid
            return;
        }
        Log.d("AudiobookPlayer", "Title: " + title + ", Author: " + author + ", FilePath: " + filePath);
        savedPosition = loadBookmark(this, filePath);
        // Set the title and author for the audiobook
        tvTitle.setText(title);
        tvAuthor.setText(author);

        // Handle Play Button
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null; // Reset MediaPlayer
            }

            // Initialize MediaPlayer and start playback
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(filePath);  // Set file path for new audiobook
                mediaPlayer.prepare();  // Prepare the MediaPlayer
                mediaPlayer.start();  // Start the playback
                seekBar.setMax(mediaPlayer.getDuration()); // Set the maximum duration
                seekBar.setProgress(mediaPlayer.getCurrentPosition()); // Set initial progress

                // Seek to saved bookmark position
                mediaPlayer.seekTo((int) savedPosition);

                // Update the seek bar position every second
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            int currentPosition = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currentPosition);
                            handler.postDelayed(this, 1000); // Update every second
                        }
                    }
                }, 1000);

                Toast.makeText(AudiobookPlayerActivity.this, "Playing", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(AudiobookPlayerActivity.this, "Error playing audiobook.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Pause Button
        btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                savedPosition = mediaPlayer.getCurrentPosition(); // Save current position
                bookmarkManager.saveBookmark(filePath, savedPosition); // Save position
                mediaPlayer.pause();
                Toast.makeText(AudiobookPlayerActivity.this, "Paused", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Stop Button
        btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                savedPosition = 0; // Reset position
                bookmarkManager.saveBookmark(filePath, savedPosition); // Save position
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(AudiobookPlayerActivity.this, "Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) { // Check if there's a previous audiobook
                currentIndex--; // Move to the previous audiobook
                switchAudiobook();
            } else {
                Toast.makeText(this, "No previous audiobook", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentIndex < audiobookList.size() - 1) { // Check if there's a next audiobook
                currentIndex++; // Move to the next audiobook
                switchAudiobook();
            } else {
                Toast.makeText(this, "No next audiobook", Toast.LENGTH_SHORT).show();
            }
        });

        // SeekBar listener to update media player position
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);  // Seek to the new position
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void switchAudiobook() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null; // Reset MediaPlayer
        }

        // Get the new audiobook
        Audiobook currentAudiobook = audiobookList.get(currentIndex);
        title = currentAudiobook.getTitle();
        author = currentAudiobook.getAuthor();
        filePath = currentAudiobook.getFilePath();

        // Update UI
        tvTitle.setText(title);
        tvAuthor.setText(author);

        // Load the new audiobook
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0); // Reset SeekBar

            // Update SeekBar
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        handler.postDelayed(this, 1000);
                    }
                }
            }, 1000);

            Toast.makeText(this, "Playing: " + title, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing audiobook.", Toast.LENGTH_SHORT).show();
        }
    }
    // Load saved bookmark position
    private long loadBookmark(Context context, String filePath) {
        return bookmarkManager.loadBookmark(filePath);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

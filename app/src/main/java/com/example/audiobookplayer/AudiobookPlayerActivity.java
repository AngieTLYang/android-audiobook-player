package com.example.audiobookplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class AudiobookPlayerActivity extends AppCompatActivity {
    private static final String TAG = "AudiobookPlayerActivity";
    private String currentFilePath;
    private AudioBookPlayerService audiobookService;
    private boolean isBound = false;
    private BookmarkManager bookmarkManager; // Declare the bookmarkManager

    private TextView tvTitle, tvAuthor;
    private ImageButton btnPlayFromBookmark, btnPlay, btnPause, btnStop, btnPrev, btnNext, btnBookmark;
    private SeekBar seekBar;

    private Handler handler = new Handler();
    private ArrayList<Audiobook> audiobookList;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_player);

        // Initialize UI elements
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        btnPlayFromBookmark = findViewById(R.id.btnPlayFromBookmark);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);

        // Initialize bookmark manager
        bookmarkManager = new BookmarkManager(this);
        bookmarkManager.open();

        // Set up seekbar
        seekBar = findViewById(R.id.seekBar);

        // Set up the bookmark button
        btnBookmark = findViewById(R.id.btnBookmark);

        // Retrieve the file path from the intent
        Intent intent = getIntent();
        currentFilePath = intent.getStringExtra("filePath");

        // Use an AtomicLong to allow reassignment
        AtomicLong currentPosition = new AtomicLong(bookmarkManager.loadBookmark(currentFilePath));

        // Handle bookmark button click to save the current position
        btnBookmark.setOnClickListener(v -> {
            currentPosition.set(seekBar.getProgress());
            bookmarkManager.saveBookmark(currentFilePath, currentPosition.get());
            Toast.makeText(AudiobookPlayerActivity.this, "Bookmark saved!", Toast.LENGTH_SHORT).show();
        });

        // Retrieve audiobook list and current index
        intent = getIntent();
        audiobookList = intent.getParcelableArrayListExtra("audiobookList");
        currentIndex = intent.getIntExtra("currentIndex", 0);

        if (audiobookList == null || audiobookList.isEmpty()) {
            Toast.makeText(this, "No audiobooks available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Start and bind to the service
        Intent serviceIntent = new Intent(this, AudioBookPlayerService.class);
        startService(serviceIntent);  // Ensures the service is running in the foreground
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        btnPlayFromBookmark.setOnClickListener(v -> {
            if (isBound) {
                audiobookService.playAudiobook(currentIndex, true, currentPosition.get());
                updateUI();
            }
        });

        // Set button click listeners
        btnPlay.setOnClickListener(v -> {
            if (isBound) {
                if (audiobookService.isPaused) {
                    audiobookService.resumeAudiobook();
                } else {
                    audiobookService.playAudiobook(currentIndex, false, 0);
                }
                updateUI();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (isBound) {
                audiobookService.pauseAudiobook();
            }
        });

        btnStop.setOnClickListener(v -> {
            if (isBound) {
                audiobookService.stopAudiobook();
                updateUI();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (isBound && currentIndex > 0) {
                currentIndex--;
                audiobookService.playAudiobook(currentIndex, false, 0);
                updateUI();
            } else {
                Toast.makeText(this, "No previous audiobook.", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (isBound && currentIndex < audiobookList.size() - 1) {
                currentIndex++;
                audiobookService.playAudiobook(currentIndex, false, 0);
                updateUI();
            } else {
                Toast.makeText(this, "No next audiobook.", Toast.LENGTH_SHORT).show();
            }
        });

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    audiobookService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update SeekBar position periodically
        handler.post(updateSeekBar);
    }

    // Service connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioBookPlayerService.LocalBinder binder = (AudioBookPlayerService.LocalBinder) service;
            audiobookService = binder.getService();
            isBound = true;

            // Set the audiobook list and current index
            audiobookService.setAudiobookList(audiobookList, currentIndex);

            // Play the audiobook if it's the first time
            audiobookService.playAudiobook(currentIndex, false, 0);
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audiobookService = null;
            isBound = false;
        }
    };

    // Update SeekBar position
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (isBound && audiobookService.isPlaying()) {
                seekBar.setProgress(audiobookService.getCurrentPosition());
                seekBar.setMax(audiobookService.getDuration());
            }
            handler.postDelayed(this, 1000); // Update every second
        }
    };

    // Update UI elements
    private void updateUI() {
        if (isBound) {
            Audiobook currentAudiobook = audiobookList.get(currentIndex);
            tvTitle.setText(currentAudiobook.getTitle());
            tvAuthor.setText(currentAudiobook.getAuthor());
            seekBar.setMax(audiobookService.getDuration());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}

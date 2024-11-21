package com.example.audiobookplayer;
import static com.example.audiobookplayer.Audiobook.loadBookmark;
import static com.example.audiobookplayer.Audiobook.saveBookmark;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class AudiobookPlayer extends RecyclerView.Adapter<AudiobookPlayer.AudiobookViewHolder> {
    private long savedPosition = 0; // Save the position when paused or stopped
    // private Audiobook.AudiobookPlayerState state;
    private List<Audiobook> audiobookList; // List of audiobooks
    private OnAudiobookClickListener listener; // Listener for item clicks
    private BookmarkManager bookmarkManager;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    // Interface to handle audiobook play button clicks
    public interface OnAudiobookClickListener {
        void onAudiobookClick(Audiobook audiobook);
    }

    // Constructor to initialize the audiobook list and listener
    public AudiobookPlayer(List<Audiobook> audiobookList, OnAudiobookClickListener listener) {
        this.audiobookList = audiobookList;
        this.listener = listener;
        this.handler = new Handler();
    }

    // Inflate the item layout and create a ViewHolder
    @NonNull
    @Override
    public AudiobookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audiobook, parent, false);
        return new AudiobookViewHolder(view);
    }

    // Bind data to the views for each item
    @Override
    public void onBindViewHolder(@NonNull AudiobookViewHolder holder, int position) {
        Audiobook audiobook = audiobookList.get(position);

        // Set title and author
        holder.tvTitle.setText(audiobook.getTitle());
        holder.tvAuthor.setText(audiobook.getAuthor());
        Context context = holder.itemView.getContext();
        this.bookmarkManager = new BookmarkManager(context);
        this.bookmarkManager.open();
        // Load the saved bookmark position for this audiobook
        savedPosition = loadBookmark(holder.itemView.getContext(), audiobook.getFilePath());
        // Handle Play Button
        holder.btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                // If there is an audiobook playing, stop it first
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null; // Reset MediaPlayer
            }
/*
Summary:
Call release() if you want to completely free up system resources and you're done with the MediaPlayer for that session. But this requires creating a new MediaPlayer every time you want to play a new audio.
Call reset() if you're planning to reuse the same MediaPlayer instance but just need to reset it to handle a new audio file. This is more efficient if you want to keep using the same instance.
*/
            // Initialize a new MediaPlayer instance
            mediaPlayer = new MediaPlayer();

            try {
                String filePath = audiobook.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    mediaPlayer.setDataSource(filePath);  // Set file path for new audiobook
                    mediaPlayer.prepare();  // Prepare the MediaPlayer
                    mediaPlayer.start();  // Start the playback
                    holder.seekBar.setMax(mediaPlayer.getDuration()); // Set the maximum duration
                    holder.seekBar.setProgress(mediaPlayer.getCurrentPosition()); // Set initial progress
                    // Seek to saved bookmark position if available (cast long to int)
                    // mediaPlayer.seekTo((int) savedPosition);
                    mediaPlayer.seekTo((int) bookmarkManager.loadBookmark(filePath)); // Load saved position
                    // int duration = mediaPlayer.getDuration();
                    holder.seekBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                holder.seekBar.setProgress(currentPosition);
                                holder.seekBar.postDelayed(this, 1000); // Update every second
                            }
                        }
                    }, 1000);
                    // Show Toast message
                    Toast.makeText(v.getContext(), "Playing: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "Invalid file path.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), "Error playing audiobook.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Pause Button
        holder.btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                savedPosition = mediaPlayer.getCurrentPosition(); // Save current position
                // saveBookmark(v.getContext(), audiobook.getFilePath(), savedPosition); // Save bookmark
                bookmarkManager.saveBookmark(audiobook.getFilePath(), savedPosition); // Save position
                mediaPlayer.pause();
                Toast.makeText(v.getContext(), "Paused: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Stop Button
        holder.btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                savedPosition = 0; // Set the position to 0 (start of the track)
                //saveBookmark(v.getContext(), audiobook.getFilePath(), savedPosition); // Save bookmark
                bookmarkManager.saveBookmark(audiobook.getFilePath(), savedPosition); // Save position
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(v.getContext(), "Stopped: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnSkip.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                //saveBookmark(v.getContext(), audiobookList.get(position).getFilePath(), mediaPlayer.getCurrentPosition());
                bookmarkManager.saveBookmark(audiobook.getFilePath(), savedPosition); // Save position
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null; // Release the current MediaPlayer
            }

            // Get the next audiobook position
            int nextPosition = position + 1;

            // Check if the next position is within bounds
            if (nextPosition < audiobookList.size()) {
                Audiobook nextAudiobook = audiobookList.get(nextPosition);

                try {
                    mediaPlayer = new MediaPlayer();
                    String filePath = nextAudiobook.getFilePath();

                    if (filePath != null && !filePath.isEmpty()) {
                        mediaPlayer.setDataSource(filePath); // Set the next audiobook file
                        mediaPlayer.prepare();
                        // Load the saved bookmark for the next audiobook, if available
                        long savedPosition = loadBookmark(v.getContext(), filePath);
                        mediaPlayer.seekTo((int) savedPosition); // Seek to the saved position (or 0 if no bookmark)
                        mediaPlayer.start();
                        Toast.makeText(v.getContext(), "Playing: " + nextAudiobook.getTitle(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Invalid file path for the next audiobook.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "Error playing the next audiobook.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If there are no more audiobooks, show a message
                Toast.makeText(v.getContext(), "No more audiobooks to skip to.", Toast.LENGTH_SHORT).show();
            }
        });
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    // Return the total number of audiobooks
    @Override
    public int getItemCount() {
        return audiobookList.size();
    }

    public void load(String filePath, float speed) {

        try {
            // Configure MediaPlayer
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            mediaPlayer.prepare();
        } catch (IOException | IllegalArgumentException e) {
            Log.e("AudiobookPlayer", "Error loading audiobook: " + e.toString());
            e.printStackTrace();
            // state = Audiobook.AudiobookPlayerState.ERROR;
        }
    }

    // ViewHolder to represent each item
    public static class AudiobookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;
        ImageButton btnPlay, btnPause, btnStop, btnSkip;
        SeekBar seekBar;
        public AudiobookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnPause = itemView.findViewById(R.id.btnPause);
            btnStop = itemView.findViewById(R.id.btnStop);
            btnSkip = itemView.findViewById(R.id.btnNext);
            seekBar = itemView.findViewById(R.id.seekBar);
        }
    }
}
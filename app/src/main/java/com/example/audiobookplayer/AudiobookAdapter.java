package com.example.audiobookplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class AudiobookAdapter extends RecyclerView.Adapter<AudiobookAdapter.AudiobookViewHolder> {
    private long savedPosition = 0; // Save the position when paused or stopped
    private AudiobookPlayer.AudiobookPlayerState state = AudiobookPlayer.AudiobookPlayerState.STOPPED;
    private List<Audiobook> audiobookList; // List of audiobooks
    private Context context;
    private MediaPlayer mediaPlayer;
    private OnAudiobookClickListener listener; // Listener for item clicks
    // Interface to handle audiobook play button clicks
    public interface OnAudiobookClickListener {
        void onAudiobookClick(Audiobook audiobook);
    }

    // Constructor to initialize the audiobook list and listener
    public AudiobookAdapter(List<Audiobook> audiobookList, OnAudiobookClickListener listener) {
        this.audiobookList = audiobookList;
        this.listener = listener;
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

        // Set title and author
        holder.tvTitle.setText(audiobook.getTitle());
        holder.tvAuthor.setText(audiobook.getAuthor());

        // Set title and author
        holder.tvTitle.setText(audiobook.getTitle());
        holder.tvAuthor.setText(audiobook.getAuthor());

        // Load the saved bookmark position for this audiobook
        savedPosition = Audiobook.loadBookmark(holder.itemView.getContext(), audiobook.getFilePath());

        // Handle Play Button
        holder.btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                // If there is an audiobook playing, stop it first
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null; // Reset MediaPlayer
            }

            // Initialize a new MediaPlayer instance
            mediaPlayer = new MediaPlayer();

            try {
                String filePath = audiobook.getFilePath();
                if (filePath != null && !filePath.isEmpty()) {
                    mediaPlayer.setDataSource(filePath);  // Set file path for new audiobook
                    mediaPlayer.prepare();  // Prepare the MediaPlayer
                    mediaPlayer.start();  // Start the playback

                    // Seek to saved bookmark position if available (cast long to int)
                    mediaPlayer.seekTo((int) savedPosition);

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
                Audiobook currentAudiobook = audiobookList.get(position);
                currentAudiobook.setBookmarkPosition(savedPosition); // Set the bookmark for current audiobook
                currentAudiobook.saveBookmark(v.getContext()); // Save bookmark to SharedPreferences

                mediaPlayer.pause();
                Toast.makeText(v.getContext(), "Paused: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Stop Button
        holder.btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                savedPosition = 0; // Set the position to 0 (start of the track)
                Audiobook currentAudiobook = audiobookList.get(position);
                currentAudiobook.setBookmarkPosition(savedPosition); // Set the bookmark for current audiobook
                currentAudiobook.saveBookmark(v.getContext()); // Save bookmark to SharedPreferences

                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(v.getContext(), "Stopped: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnSkip.setOnClickListener(v -> {
            if (mediaPlayer != null) {
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
    }

    // Return the total number of audiobooks
    @Override
    public int getItemCount() {
        return audiobookList.size();
    }

    // ViewHolder to represent each item
    public static class AudiobookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;
        ImageButton btnPlay, btnPause, btnStop, btnSkip;

        public AudiobookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnPause = itemView.findViewById(R.id.btnPause);
            btnStop = itemView.findViewById(R.id.btnStop);
            btnSkip = itemView.findViewById(R.id.btnSkip);
        }
    }

    // Method to start playing
    private void startPlaying(Audiobook audiobook, Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release any previously playing audio
        }

        try {
            mediaPlayer = new MediaPlayer();
            String filePath = audiobook.getFilePath();

            if (filePath != null && !filePath.isEmpty()) {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();

                // Set the state to PLAYING
                state = AudiobookPlayer.AudiobookPlayerState.PLAYING;
                Toast.makeText(context, "Playing: " + audiobook.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                state = AudiobookPlayer.AudiobookPlayerState.ERROR;  // Set state to ERROR if file path is invalid
                Toast.makeText(context, "Invalid file path.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            state = AudiobookPlayer.AudiobookPlayerState.ERROR;  // Set state to ERROR if something goes wrong
            Toast.makeText(context, "Error playing audiobook.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to pause playing
    private void pausePlaying(Context context) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            state = AudiobookPlayer.AudiobookPlayerState.PAUSED;  // Set state to PAUSED
            Toast.makeText(context, "Paused", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to stop playing
    private void stopPlaying(Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            state = AudiobookPlayer.AudiobookPlayerState.STOPPED;  // Set state to STOPPED
            Toast.makeText(context, "Stopped", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.example.audiobookplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class AudiobookPlayerAdapter extends RecyclerView.Adapter<AudiobookPlayerAdapter.AudiobookViewHolder> {

    private Integer selectedColor;
    private List<Audiobook> audiobookList; // List of audiobooks
    private OnAudiobookClickListener listener; // Listener for item clicks
    private MediaPlayer mediaPlayer;
    // private BookmarkManager bookmarkManager; // Declare the bookmarkManager
    // Interface to handle audiobook item clicks
    public interface OnAudiobookClickListener {
        void onAudiobookClick(Audiobook audiobook);
    }

    // Constructor to initialize the audiobook list and listener
    public AudiobookPlayerAdapter(List<Audiobook> audiobookList, OnAudiobookClickListener listener, Context context) {
        this.audiobookList = audiobookList;
        this.listener = listener;
        // Load the saved color from SharedPreferences
        SharedPreferences sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.selectedColor = sharedPref.getInt("selectedColor", Color.WHITE); // Default color is white
        // this.bookmarkManager = bookmarkManager; // Initialize it
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

        // Retrieve saved position from bookmark manager
        String filePath = audiobook.getFilePath(); // Ensure Audiobook has a method to get file path
        // long savedPosition = bookmarkManager.loadBookmark(filePath); // Load saved bookmark position

        // Format saved position to a timestamp (HH:mm:ss) and set it in a TextView
        // String formattedTimestamp = formatTime(savedPosition);
        //holder.tvBookmarkTimestamp.setText(formattedTimestamp); // tvTimestamp should be defined in the ViewHolder

        // Set background color of cardView
        if (holder.cardView != null) {
            holder.cardView.setBackgroundColor(selectedColor);
        } else {
            Log.e("AudiobookAdapter", "CardView is null for position: " + position);
        }

        // Set a click listener to open the AudiobookPlayerActivity for the selected audiobook
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAudiobookClick(audiobook);
            }
        });
    }

    // Utility method to format milliseconds to HH:mm:ss
    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void updateColor(int color) {
        this.selectedColor = color;
        notifyDataSetChanged(); // Refresh the list to apply the new color
    }

    // Return the total number of audiobooks
    @Override
    public int getItemCount() {
        return audiobookList.size();
    }

    // ViewHolder to represent each item in the list
    public static class AudiobookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvBookmarkTimestamp;
        ConstraintLayout cardView;
        public AudiobookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            cardView = itemView.findViewById(R.id.cardView);
            tvBookmarkTimestamp = itemView.findViewById(R.id.tvBookmarkTimestamp);
        }
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
}

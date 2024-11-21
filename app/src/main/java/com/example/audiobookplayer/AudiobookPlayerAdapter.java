package com.example.audiobookplayer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class AudiobookPlayerAdapter extends RecyclerView.Adapter<AudiobookPlayerAdapter.AudiobookViewHolder> {

    private List<Audiobook> audiobookList; // List of audiobooks
    private OnAudiobookClickListener listener; // Listener for item clicks
    private MediaPlayer mediaPlayer;
    // Interface to handle audiobook item clicks
    public interface OnAudiobookClickListener {
        void onAudiobookClick(Audiobook audiobook);
    }

    // Constructor to initialize the audiobook list and listener
    public AudiobookPlayerAdapter(List<Audiobook> audiobookList, OnAudiobookClickListener listener) {
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

        // Set a click listener to open the AudiobookPlayerActivity for the selected audiobook
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAudiobookClick(audiobook);
            }
        });
    }

    // Return the total number of audiobooks
    @Override
    public int getItemCount() {
        return audiobookList.size();
    }

    // ViewHolder to represent each item in the list
    public static class AudiobookViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;

        public AudiobookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
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

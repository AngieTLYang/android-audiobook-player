package com.example.audiobookplayer;

import android.content.Context;
import android.content.SharedPreferences;
public class Audiobook {
    private String title;
    private String author;
    private String filePath;
    private long bookmarkPosition;
    // Enum to represent player states
    public enum AudiobookPlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }
    private AudiobookPlayerState state;
    public Audiobook(String title, String author, String filePath, long bookmarkPosition) {
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.bookmarkPosition = bookmarkPosition;
        this.state = AudiobookPlayerState.STOPPED;
    }

    // Getter and Setter for the player state
    public AudiobookPlayerState getState() {
        return state;
    }

    public void setState(AudiobookPlayerState state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getBookmarkPosition() {
        return bookmarkPosition;  // Get the bookmark position
    }

    public void setBookmarkPosition(long bookmarkPosition) {
        this.bookmarkPosition = bookmarkPosition;  // Set the bookmark position
    }
    // Save the bookmark position to SharedPreferences
    public static void saveBookmark(Context context, String filePath, long position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AudiobookPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Ensure filePath is sanitized for storage, if necessary
        // String key = "bookmark_" + this.getFilePath();
        // editor.putLong(key, this.getBookmarkPosition());
        editor.putLong(filePath, position);
        editor.apply(); // Apply the changes asynchronously
    }

    // Load the bookmark position from SharedPreferences
    public static long loadBookmark(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AudiobookPreferences", Context.MODE_PRIVATE);
        //String key = "bookmark_" + filePath; // Ensure the key format is the same as when saving
        //return sharedPreferences.getLong(key, 0);  // Default to 0 if no bookmark is saved
        return sharedPreferences.getLong(filePath, 0);
    }
}

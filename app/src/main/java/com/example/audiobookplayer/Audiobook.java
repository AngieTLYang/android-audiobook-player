package com.example.audiobookplayer;

import android.content.Context;
import android.content.SharedPreferences;
public class Audiobook {
    private String title;
    private String author;
    private String filePath;
    private long bookmarkPosition;

    public Audiobook(String title, String author, String filePath, long bookmarkPosition) {
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.bookmarkPosition = bookmarkPosition;
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
    public void saveBookmark(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AudiobookPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("bookmark_" + this.getFilePath(), this.getBookmarkPosition());
        editor.apply();
    }

    // Load the bookmark position from SharedPreferences
    public static long loadBookmark(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AudiobookPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("bookmark_" + filePath, 0); // Default to 0 if no bookmark is saved
    }
}

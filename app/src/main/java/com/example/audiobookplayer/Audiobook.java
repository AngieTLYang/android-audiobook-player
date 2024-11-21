package com.example.audiobookplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class Audiobook implements Parcelable {
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

    // Constructor used for Parcel
    protected Audiobook(Parcel in) {
        title = in.readString();
        author = in.readString();
        filePath = in.readString();
        bookmarkPosition = in.readLong();
        state = AudiobookPlayerState.values()[in.readInt()]; // Read the state from the Parcel
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
        return bookmarkPosition;
    }

    public void setBookmarkPosition(long bookmarkPosition) {
        this.bookmarkPosition = bookmarkPosition;
    }

    // Implement the writeToParcel method
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(filePath);
        dest.writeLong(bookmarkPosition);
        dest.writeInt(state.ordinal()); // Write the state (ordinal) to the Parcel
    }

    @Override
    public int describeContents() {
        return 0;  // No special contents to describe
    }

    // Creator for Parcelable
    public static final Creator<Audiobook> CREATOR = new Creator<Audiobook>() {
        @Override
        public Audiobook createFromParcel(Parcel in) {
            return new Audiobook(in);
        }

        @Override
        public Audiobook[] newArray(int size) {
            return new Audiobook[size];
        }
    };
}

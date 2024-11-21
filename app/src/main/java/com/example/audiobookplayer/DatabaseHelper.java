package com.example.audiobookplayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "audiobooks.db";
    private static final int DATABASE_VERSION = 2;

    // Table name and columns
    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COLUMN_FILE_PATH = "filePath";
    public static final String COLUMN_POSITION = "position";

    // SQL statement to create the bookmarks table
    private static final String CREATE_TABLE_BOOKMARKS =
            "CREATE TABLE " + TABLE_BOOKMARKS + " (" +
                    COLUMN_FILE_PATH + " TEXT PRIMARY KEY, " +
                    COLUMN_POSITION + " INTEGER);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL to create the bookmarks table
        db.execSQL(CREATE_TABLE_BOOKMARKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table and recreate it if the database version changes
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        onCreate(db);
    }
    public long getBookmark(String filePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("bookmarks", new String[]{"position"}, "filePath=?",
                new String[]{filePath}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            long position = cursor.getLong(0);
            cursor.close();
            return position;
        }
        if (cursor != null) cursor.close();
        return 0L; // Default to 0 if no bookmark is found
    }
}

package com.example.audiobookplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.audiobookplayer.DatabaseHelper;

public class BookmarkManager {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public BookmarkManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Save the bookmark position for the audiobook
    public void saveBookmark(String filePath, long position) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FILE_PATH, filePath);
        values.put(DatabaseHelper.COLUMN_POSITION, position);

        long result = database.insertWithOnConflict(
                DatabaseHelper.TABLE_BOOKMARKS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Load the saved bookmark position for the audiobook
    public long loadBookmark(String filePath) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_BOOKMARKS,
                new String[]{DatabaseHelper.COLUMN_POSITION},
                DatabaseHelper.COLUMN_FILE_PATH + " = ?",
                new String[]{filePath},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Get column index safely
                int positionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_POSITION);

                // Check if the column index is valid (not -1)
                if (positionIndex != -1) {
                    long position = cursor.getLong(positionIndex);
                    cursor.close();
                    return position;
                }
            }
            cursor.close();
        }
        return 0; // If no bookmark is found, return 0
    }
}

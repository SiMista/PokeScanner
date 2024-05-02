package com.example.pokescanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ImagesDataSource {

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public ImagesDataSource(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertImage(String imagePath) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IMAGE_PATH, imagePath);
        return database.insert(DatabaseHelper.TABLE_IMAGES, null, values);
    }

    public Cursor getAllImages() {
        return database.query(DatabaseHelper.TABLE_IMAGES,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_IMAGE_PATH},
                null, null, null, null, null);
    }
}

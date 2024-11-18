package com.example.cwk_mwe;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility class providing methods for managing bookmarks including saving and loading bookmarks
 * from a local JSON file, bookmarks.JSON, found in
 * /data/data/com.example.cwk_mwe/files/bookmarks.json
 * This file is in the apps internal storage and should be private to the app.
 */
public class BookmarkManager {
    private static final String BOOKMARKS_FILE = "bookmarks.json";

    public static boolean addBookmark(Context context, BookmarkData bookmark) {
        try {
            List<BookmarkData> bookmarks = loadBookmarks(context);
            bookmarks.add(bookmark);
            saveBookmarks(context, bookmarks);
            Log.d("BookmarkManager", "Bookmark added successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<BookmarkData> loadBookmarks(Context context) throws IOException {
        File file = new File(context.getFilesDir(), BOOKMARKS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<BookmarkData>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        }
    }

    private static void saveBookmarks(Context context, List<BookmarkData> bookmarks) throws IOException {
        File file = new File(context.getFilesDir(), BOOKMARKS_FILE);
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(bookmarks, writer);
        }
    }
}

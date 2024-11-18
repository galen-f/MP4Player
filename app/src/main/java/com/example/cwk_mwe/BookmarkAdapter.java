package com.example.cwk_mwe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private final List<BookmarkData> bookmarks;
    private final OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(BookmarkData bookmark);
    }

    public BookmarkAdapter(List<BookmarkData> bookmarks, OnBookmarkClickListener listener) {
        this.bookmarks = bookmarks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookmarkData bookmark = bookmarks.get(position);
        holder.titleView.setText(bookmark.getTitle());
        holder.timestampView.setText(formatTimestamp(bookmark.getTimestamp()));

        holder.itemView.setOnClickListener(v -> listener.onBookmarkClick(bookmark));
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    private String formatTimestamp(long timestamp) {
        int seconds = (int) (timestamp / 1000) % 60;
        int minutes = (int) ((timestamp / (1000 * 60)) % 60);
        int hours = (int) ((timestamp / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleView;
        public final TextView timestampView;

        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(android.R.id.text1);
            timestampView = view.findViewById(android.R.id.text2);
        }
    }
}

package com.example.cwk_mwe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    private List<TrackData> trackData;
    private Context context;
    private OnItemClickListener listener;

    // Define a listener interface for click handling
    public interface OnItemClickListener {
        void onItemClick(String filePath);
    }

    // Constructor with listener as third argument
    public MusicRecyclerViewAdapter(Context context, List<TrackData> trackData, OnItemClickListener listener) {
        this.context = context;
        this.trackData = trackData;
        this.listener = listener;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        TrackData trackDataItem = trackData.get(position);
        holder.textView.setText(trackDataItem.fileName);

        // Set the click listener for the item view
        holder.itemView.setOnClickListener(v -> listener.onItemClick(trackDataItem.filePath));
    }

    @Override
    public int getItemCount() {
        return trackData.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MusicViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}

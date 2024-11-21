package com.example.cwk_mwe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.databinding.TrackItemBinding;

import java.util.List;

/**
 * Adapter for displaying a list of audio tracks in a RecyclerView within the main activity.
 * Facilitates interaction with track items, including navigation to the player screen upon selection.
 */

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    public List<TrackData> trackData;
    private OnItemClickListener listener;

    // Define a listener interface for click handling
    public interface OnItemClickListener {
        void onItemClick(String filePath);
    }

    // Constructor with listener as third argument
    public MusicRecyclerViewAdapter(List<TrackData> trackData, OnItemClickListener listener) {
        this.trackData = trackData;
        this.listener = listener;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TrackItemBinding binding = TrackItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MusicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        // Bind the data and listener to the ViewHolder
        TrackData track = trackData.get(position);
        holder.bind(track, listener);
    }

    @Override
    public int getItemCount() {
        return trackData.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {

        private final TrackItemBinding binding;

        public MusicViewHolder(TrackItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TrackData track, OnItemClickListener listener) {
            binding.setTrack(track);
            binding.setClickListener(listener);
            binding.executePendingBindings(); // Ensures the binding happens immediately
        }
    }
}

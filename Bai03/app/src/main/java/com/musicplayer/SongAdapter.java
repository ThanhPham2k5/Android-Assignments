package com.musicplayer;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<SongModel> songs;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(SongModel song);
        void onSelectCover(SongModel song, int position);
    }
    public SongAdapter(List<SongModel> songs, OnItemClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(long duration) {
        int minutes = (int) (duration / 1000) / 60;
        int seconds = (int) (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        SongModel song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText("Artist: " + song.getArtist());
        holder.duration.setText("Duration: " + formatDuration(song.getDuration()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(song));

        //load img with glide
        Glide.with(holder.songCover.getContext())
                        .load(song.getCoverUri())
                                .placeholder(R.drawable.ic_music_note)
                                        .into(holder.songCover);

        holder.songCover.setOnLongClickListener(v -> {
            listener.onSelectCover(song, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, duration;
        ImageView songCover;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            duration = itemView.findViewById(R.id.songDuration);
            songCover = itemView.findViewById(R.id.songCover);
        }
    }
}

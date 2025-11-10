package com.example.myimagegallery;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.PhotoViewHolder> {

    private List<ImageItem> imageList;

    public ImagePagerAdapter(List<ImageItem> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_view, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri uri = imageList.get(position).getImageUrl();
        Glide.with(holder.photoView.getContext())
                .load(uri)
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
        }
    }
}

package com.example.myimagegallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// Tạo adapter để đưa ảnh vào RecyclerView
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    // Danh sách ảnh từ MediaStore.Image
    private List<ImageItem> imageItems;
    // Lắng nghe sự kiện click ảnh
    private OnItemClickListener listener;

    // Interface để giao tiếp với Activity
    public interface OnItemClickListener {
        void onItemClick(ImageItem imageItem, int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        this.listener = listener;
    }

    // Hàm constructor cho danh sách ảnh
    public ImageAdapter(List<ImageItem> imageItems) {
        this.imageItems = imageItems;
    }

    // Hàm tạo ô image_card chứa ảnh lưu vào trong ViewHolder
    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new ViewHolder(view);
    }

    // Gắn ảnh vào ViewHolder bằng Glide
    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        // Lấy vị trí ảnh
        ImageItem imageItem = imageItems.get(position);

        // Gắn ảnh vào (có crop ảnh, có thumbnail để load ảnh tạm với độ phân giải thấp)
        Glide.with(holder.imageView.getContext())
                .load(imageItem.getImageUrl())
                .centerCrop()
                .thumbnail(0.1f)
                .into(holder.imageView);

        // Xử lý click ảnh
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(imageItems.get(position), position);
            }
        });
    }

    // Trả số lượng ảnh hiện tại
    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    // Tạo ViewHolder đại diện (tham chiếu) cho một ô trong RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Ô này chứa một view hình
        ImageView imageView;

        // Hàm constructor gán view hình bằng view hình đã định nghĩa trong image_card
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
        }
    }
}

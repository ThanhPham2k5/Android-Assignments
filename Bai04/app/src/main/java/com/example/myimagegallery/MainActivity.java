package com.example.myimagegallery;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Biến recyclerview
    RecyclerView recyclerView;
    // Biến adapter đẩy danh sách ảnh vào recyclerview bằng ViewHolder
    ImageAdapter adapter;
    // Danh sách ảnh
    List<ImageItem> imageList = new ArrayList<>();

    // Biến truyền string tên permission yêu cầu
    ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo recyclerview với layout grid 3 ô
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);

        // Khởi tạo adapter với danh sách ảnh
        adapter = new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);

        // Ghi đè Interface sự kiện click ảnh
        adapter.setOnItemClickListener((imageItem, position) -> {
            Intent intent = new Intent(MainActivity.this, ImageDetail.class);
            intent.putParcelableArrayListExtra("image_list", new ArrayList<>(imageList));
            intent.putExtra("position", position);
            startActivity(intent);
        });

        // Đăng ký yêu cầu quyền trong manifest khi khởi chạy
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) loadImages();
                }
        );

        // Kiểm tra lại quyền truy cập ảnh
        checkPermission();
    }

    private void checkPermission() {
        String permission;
        // Phân loại yêu câu manifest cho phiên bản API khác nhau
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Kiểm tra quyền truy cập
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadImages();
        } else {
            // Yêu cầu truy cập lại
            permissionLauncher.launch(permission);
        }
    }

    private void loadImages() {
        // Tạo luồng riêng để xử lý việc load ảnh (tăng tốc độ, tránh lỗi)
        new Thread(() -> {
            // Danh sách ảnh tạm
            List<ImageItem> tempList = new ArrayList<>();

            // Đường dẫn uri của bộ sưu tập ảnh
            Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            // Thông tin của ảnh gồm: id, tên ảnh và ngày thêm
            String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED
            };

            // Sắp xếp theo ngày mới nhất
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

            // Chạy lệnh để lấy ành từ collection theo projection và sortOrder
            try (var cursor = getContentResolver().query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
            )) {
                if (cursor != null) {
                    // Lấy thứ tự cột id và tên trong cursor
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

                    // Duyệt từng hàng (từng ảnh một)
                    while (cursor.moveToNext()) {
                        // Lấy id và tên ảnh theo thứ tự cột tương ứng
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);

                        // Tạo uri ảnh
                        Uri contentUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                        );

                        // Thêm ảnh vào danh sách tạm
                        tempList.add(new ImageItem(contentUri, name));
                    }
                }
            }

            // Thêm vào danh sách chính và cập nhật adapter
            // Chỉ chạy trên luồng UI để tránh xung đột và nhanh
            runOnUiThread(() -> {
                imageList.clear();
                imageList.addAll(tempList);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
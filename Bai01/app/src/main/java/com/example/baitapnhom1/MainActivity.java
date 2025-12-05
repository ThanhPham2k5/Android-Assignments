package com.example.baitapnhom1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        btnEdit = findViewById(R.id.btnEdit);

        //Load dữ liệu từ SharedPreferences
        loadUserData();

        //Mở EditActivity, gửi dữ liệu hiện có
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            intent.putExtra("name", prefs.getString("name", ""));
            intent.putExtra("email", prefs.getString("email", ""));
            intent.putExtra("imageUri", prefs.getString("imageUri", ""));
            startActivityForResult(intent, 1);
        });
    }

    //Nhận dữ liệu từ EditActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String email = data.getStringExtra("email");
            String imageUri = data.getStringExtra("imageUri");

            //Cập nhật giao diện
            if (name != null) tvName.setText("Tên: " + name);
            if (email != null) tvEmail.setText("Email: " + email);

            if (imageUri != null && !imageUri.isEmpty()) {
                try {
                    Uri uri = Uri.parse(imageUri);
                    getContentResolver().openInputStream(uri); // kiểm tra quyền
                    imgAvatar.setImageURI(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Lưu dữ liệu vào SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("UserData", MODE_PRIVATE).edit();
            editor.putString("name", name != null ? name : "");
            editor.putString("email", email != null ? email : "");
            editor.putString("imageUri", imageUri != null ? imageUri : "");
            editor.apply();
        }
    }

    //Load dữ liệu khi mở app
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String savedName = prefs.getString("name", "");
        String savedEmail = prefs.getString("email", "");
        String savedImageUri = prefs.getString("imageUri", "");

        if (!savedName.isEmpty()) tvName.setText("Tên: " + savedName);
        if (!savedEmail.isEmpty()) tvEmail.setText("Email: " + savedEmail);

        if (!savedImageUri.isEmpty()) {
            try {
                Uri uri = Uri.parse(savedImageUri);
                getContentResolver().openInputStream(uri);
                imgAvatar.setImageURI(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

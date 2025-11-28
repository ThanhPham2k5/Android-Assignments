package com.example.baitapnhom1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class EditActivity extends AppCompatActivity {

    private ImageView imgEditAvatar;
    private EditText etName, etEmail;
    private Button btnSelectImage, btnSave, btnTakePhoto;
    private Uri imageUri; //Lưu URI ảnh thư viện hoặc camera (nếu lưu file)

    //Xin quyền camera
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Mở camera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    imgEditAvatar.setImageBitmap(photo);
                    imageUri = null; // ảnh camera tạm thời, không có URI
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imgEditAvatar = findViewById(R.id.imgEditAvatar);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        //LẤY DỮ LIỆU CŨ TỪ INTENT
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String imageUriStr = intent.getStringExtra("imageUri");

        if (name != null) etName.setText(name);
        if (email != null) etEmail.setText(email);
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            imageUri = Uri.parse(imageUriStr);
            try {
                getContentResolver().openInputStream(imageUri); // kiểm tra quyền
                imgEditAvatar.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Chọn ảnh từ thư viện
        btnSelectImage.setOnClickListener(v -> openGallery());

        //Chụp ảnh
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        //Lưu dữ liệu
        btnSave.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("name", etName.getText().toString().trim());
            result.putExtra("email", etEmail.getText().toString().trim());
            if (imageUri != null) result.putExtra("imageUri", imageUri.toString());
            setResult(RESULT_OK, result);
            finish();
        });
    }

    //Mở thư viện ảnh
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, 101);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý ảnh từ thư viện
        if (resultCode == RESULT_OK && requestCode == 101 && data != null) {
            imageUri = data.getData();
            imgEditAvatar.setImageURI(imageUri);

            // Giữ quyền truy cập URI vĩnh viễn
            try {
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

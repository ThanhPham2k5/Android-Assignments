package com.example.baitapnhom1;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    private Uri imageUri;        // lưu ảnh từ thư viện hoặc camera
    private Uri cameraImageUri;  // URI ảnh chụp thật (file lưu vào MediaStore)

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

        // Nhận dữ liệu cũ
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String imageUriStr = intent.getStringExtra("imageUri");

        if (name != null) etName.setText(name);
        if (email != null) etEmail.setText(email);

        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            imageUri = Uri.parse(imageUriStr);
            imgEditAvatar.setImageURI(imageUri);
        }

        // Chọn ảnh thư viện
        btnSelectImage.setOnClickListener(v -> openGallery());

        // Chụp ảnh
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Lưu dữ liệu
        btnSave.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("name", etName.getText().toString().trim());
            result.putExtra("email", etEmail.getText().toString().trim());

            if (imageUri != null)
                result.putExtra("imageUri", imageUri.toString());

            setResult(RESULT_OK, result);
            finish();
        });
    }

    // ============================
    // QUYỀN CAMERA
    // ============================
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
                else Toast.makeText(this, "Bạn cần cấp quyền Camera", Toast.LENGTH_SHORT).show();
            });

    // ============================
    // CAMERA
    // ============================
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "captured_image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image taken from camera");
        return getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void openCamera() {
        cameraImageUri = createImageUri();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

        cameraLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    imageUri = cameraImageUri; // lưu URI thật
                    imgEditAvatar.setImageURI(imageUri);
                }
            });

    // ============================
    // GALLERY
    // ============================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Chọn ảnh thư viện
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();
            imgEditAvatar.setImageURI(imageUri);

            // Lưu quyền truy cập
            final int takeFlags = data.getFlags()
                    & Intent.FLAG_GRANT_READ_URI_PERMISSION;

            getContentResolver().takePersistableUriPermission(
                    imageUri, takeFlags
            );
        }
    }
}

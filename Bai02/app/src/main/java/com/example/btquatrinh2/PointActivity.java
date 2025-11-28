package com.example.btquatrinh2;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PointActivity extends AppCompatActivity {
    EditText edtPhone, edtPointInput, edtNote;
    TextView tvCurrentPoint, tvTitle, tvLabelInput;
    Button btnSave, btnSaveNext;
    Button navInput, navUse, navList;
    DBHelper db;
    boolean isAddMode = true; // Biến cờ: true là INPUT, false là USE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        db = new DBHelper(this);
        initView();

        String mode = getIntent().getStringExtra("MODE");
        isAddMode = "ADD".equals(mode);
        setupUI();

        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCustomerInfo(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> handleSave(false));
        btnSaveNext.setOnClickListener(v -> handleSave(true));
        setupBottomNav();
    }

    private void initView() {
        edtPhone = findViewById(R.id.edtPhone);
        edtPointInput = findViewById(R.id.edtPointInput);
        edtNote = findViewById(R.id.edtNote);
        tvCurrentPoint = findViewById(R.id.tvCurrentPoint);
        tvTitle = findViewById(R.id.tvLabelInput);
        tvLabelInput = findViewById(R.id.tvLabelInput);
        btnSave = findViewById(R.id.btnSave);
        btnSaveNext = findViewById(R.id.btnSaveNext);
        navInput = findViewById(R.id.navInput);
        navUse = findViewById(R.id.navUse);
        navList = findViewById(R.id.navList);
    }

    private void setupUI() {
        int activeBg = Color.parseColor("#E3F2FD");
        int activeText = Color.parseColor("#1976D2");

        int inactiveBg = Color.parseColor("#FFFFFF");
        int inactiveText = Color.parseColor("#757575");

        if (isAddMode) {
            tvTitle.setText("INPUT POINT");
            tvLabelInput.setText("Input new point");

            navInput.setBackgroundTintList(ColorStateList.valueOf(activeBg));
            navInput.setTextColor(activeText);
            navInput.setTypeface(null, Typeface.BOLD);

            navUse.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
            navUse.setTextColor(inactiveText);
            navUse.setTypeface(null, Typeface.NORMAL);

        } else {
            tvTitle.setText("USE POINT");
            tvLabelInput.setText("Used point");

            navUse.setBackgroundTintList(ColorStateList.valueOf(activeBg));
            navUse.setTextColor(activeText);
            navUse.setTypeface(null, Typeface.BOLD);

            navInput.setBackgroundTintList(ColorStateList.valueOf(inactiveBg));
            navInput.setTextColor(inactiveText);
            navInput.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void loadCustomerInfo(String phone) {
        Customer c = db.getCustomer(phone);
        if (c != null) {
            tvCurrentPoint.setText(String.valueOf(c.points));
            edtNote.setText(c.note);
        } else {
            tvCurrentPoint.setText("0");
            edtNote.setText("");
        }
    }

    private void handleSave(boolean isNext) {
        String phone = edtPhone.getText().toString();
        String pointStr = edtPointInput.getText().toString();
        String note = edtNote.getText().toString();

        if (phone.isEmpty() || pointStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int points = Integer.parseInt(pointStr);

        db.updatePoints(phone, points, isAddMode, note);

        Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
        loadCustomerInfo(phone);

        if (isNext) {
            edtPhone.setText("");
            edtPointInput.setText("");
            edtNote.setText("");
            tvCurrentPoint.setText("");
            edtPhone.requestFocus();
        }
    }

    private void setupBottomNav() {
        navInput.setOnClickListener(v -> {
            if (!isAddMode) {
                Intent intent = new Intent(this, PointActivity.class);
                intent.putExtra("MODE", "ADD");
                startActivity(intent);
                finish();
            }
        });

        navUse.setOnClickListener(v -> {
            if (isAddMode) {
                Intent intent = new Intent(this, PointActivity.class);
                intent.putExtra("MODE", "USE");
                startActivity(intent);
                finish();
            }
        });

        navList.setOnClickListener(v -> {
            startActivity(new Intent(this, ListCustomerActivity.class));
        });
    }
}
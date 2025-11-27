package com.example.btquatrinh2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.InputStream;
import java.util.ArrayList;

public class ListCustomerActivity extends AppCompatActivity {
    RecyclerView rcvCustomer;
    CustomerAdapter adapter;
    DBHelper db;
    Button navInput, navUse, navList;
    ArrayList<Customer> list;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_customer);

        db = new DBHelper(this);

        rcvCustomer = findViewById(R.id.rcvCustomer);
        navInput = findViewById(R.id.navInput);
        navUse = findViewById(R.id.navUse);
        navList = findViewById(R.id.navList);
        Button btnImport = findViewById(R.id.btnImport);
        Button btnExport = findViewById(R.id.btnExport);

        setupFilePicker();

        list = db.getAllCustomers();

        rcvCustomer.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerAdapter(this, list, new CustomerAdapter.OnDeleteClickListener() {
            @Override
            public void onDelete(String phone, int position) {
                showDeleteConfirmation(phone, position);
            }
        });
        rcvCustomer.setAdapter(adapter);

        // Navigation
        navInput.setOnClickListener(v -> {
            Intent intent = new Intent(this, PointActivity.class);
            intent.putExtra("MODE", "ADD");
            startActivity(intent);
            finish();
        });

        navUse.setOnClickListener(v -> {
            Intent intent = new Intent(this, PointActivity.class);
            intent.putExtra("MODE", "USE");
            startActivity(intent);
            finish();
        });

        // Xử lý Export
        btnExport.setOnClickListener(v -> {
            ArrayList<Customer> currentList = db.getAllCustomers();
            XmlHandler.exportAndEmail(this, currentList);
        });

        // Xử lý Import
        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/xml");
            filePickerLauncher.launch(intent);
        });
    }

    // Hàm cài đặt cho việc chọn file Import
    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            InputStream is = getContentResolver().openInputStream(uri);
                            // Gọi hàm parseXml
                            ArrayList<Customer> importedList = XmlHandler.parseXml(is);

                            // Lưu vào DB
                            int count = 0;
                            for(Customer c : importedList) {
                                db.importCustomer(c);
                                count++;
                            }

                            // Cập nhật lại danh sách hiển thị
                            list.clear();
                            list.addAll(db.getAllCustomers());
                            adapter.notifyDataSetChanged();

                            Toast.makeText(this, "Đã import " + count + " khách!", Toast.LENGTH_SHORT).show();
                            if(is != null) is.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi Import: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void showDeleteConfirmation(String phone, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa khách hàng " + phone + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean result = db.deleteCustomer(phone);
                    if (result) {
                        list.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, list.size());
                        Toast.makeText(ListCustomerActivity.this, "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ListCustomerActivity.this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
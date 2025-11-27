package com.example.btquatrinh2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DB_NAME = "LoyaltyApp.db";
    private static final int DB_VERSION = 1;

    // Table Customer
    private static final String TABLE_CUSTOMER = "customers";
    private static final String COL_PHONE = "phone";
    private static final String COL_POINTS = "points";
    private static final String COL_CREATED = "created_at";
    private static final String COL_UPDATED = "updated_at";
    private static final String COL_NOTE = "note";

    // Table Admin (Cho login)
    private static final String TABLE_ADMIN = "admin";
    private static final String COL_USER = "username";
    private static final String COL_PASS = "password";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Customer
        String createCus = "CREATE TABLE " + TABLE_CUSTOMER + " (" +
                COL_PHONE + " TEXT PRIMARY KEY, " +
                COL_POINTS + " INTEGER DEFAULT 0, " +
                COL_CREATED + " TEXT, " +
                COL_UPDATED + " TEXT, " +
                COL_NOTE + " TEXT)";
        db.execSQL(createCus);

        // Tạo bảng Admin và insert user mặc định
        db.execSQL("CREATE TABLE " + TABLE_ADMIN + " (" + COL_USER + " TEXT PRIMARY KEY, " + COL_PASS + " TEXT)");
        db.execSQL("INSERT INTO " + TABLE_ADMIN + " VALUES ('admin', '123456')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
        onCreate(db);
    }

    // --- CHỨC NĂNG LOGIN & ĐỔI PASS ---
    public boolean checkLogin(String user, String pass) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ADMIN + " WHERE " + COL_USER + "=? AND " + COL_PASS + "=?", new String[]{user, pass});
        boolean result = c.getCount() > 0;
        c.close();
        return result;
    }

    public void changePassword(String user, String newPass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASS, newPass);
        db.update(TABLE_ADMIN, values, COL_USER + "=?", new String[]{user});
    }

    // --- CHỨC NĂNG KHÁCH HÀNG ---
    public Customer getCustomer(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_CUSTOMER, null, COL_PHONE + "=?", new String[]{phone}, null, null, null);
        if (c != null && c.moveToFirst()) {
            Customer cus = new Customer(c.getString(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4));
            c.close();
            return cus;
        }
        return null;
    }

    public boolean deleteCustomer(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete trả về số dòng bị ảnh hưởng. Nếu > 0 là xóa thành công
        long result = db.delete(TABLE_CUSTOMER, COL_PHONE + "=?", new String[]{phone});
        return result > 0;
    }

    public void updatePoints(String phone, int pointChange, boolean isAdd, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        Customer current = getCustomer(phone);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put(COL_UPDATED, date);

        if (current == null) {
            // Khách mới -> Tạo mới (Chỉ khi là chức năng Cộng điểm)
            if (isAdd) {
                values.put(COL_PHONE, phone);
                values.put(COL_POINTS, pointChange);
                values.put(COL_NOTE, note);
                values.put(COL_CREATED, date);
                db.insert(TABLE_CUSTOMER, null, values);
            }
        } else {
            // Khách cũ -> Cập nhật
            int newPoints = isAdd ? (current.points + pointChange) : (current.points - pointChange);
            if (newPoints < 0) newPoints = 0; // Không cho âm điểm
            values.put(COL_POINTS, newPoints);
            db.update(TABLE_CUSTOMER, values, COL_PHONE + "=?", new String[]{phone});
        }
    }

    public ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_CUSTOMER, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Customer(c.getString(0), c.getInt(1), c.getString(2), c.getString(3), c.getString(4)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // Hàm hỗ trợ Import XML (Xóa cũ thêm mới hoặc update đè)
    public void importCustomer(Customer cus) {
        // Logic tương tự updatePoints nhưng set cứng giá trị điểm
        // Viết gọn cho ví dụ:
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PHONE, cus.phone);
        values.put(COL_POINTS, cus.points);
        values.put(COL_CREATED, cus.createdDate);
        values.put(COL_UPDATED, cus.updatedDate);
        // Insert or Update
        long result = db.insertWithOnConflict(TABLE_CUSTOMER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}

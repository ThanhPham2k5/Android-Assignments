package com.example.btquatrinh2;

public class Customer {
    public String phone;
    public int points;
    public String createdDate;
    public String updatedDate;
    public String note;

    public Customer(String phone, int points, String createdDate, String updatedDate, String note) {
        this.phone = phone;
        this.points = points;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.note = note;
    }
}

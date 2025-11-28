package com.example.btquatrinh2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    Context context;
    ArrayList<Customer> list;
    OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDelete(String phone, int position);
    }

    public CustomerAdapter(Context context, ArrayList<Customer> list, OnDeleteClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer c = list.get(position);
        holder.tvPhone.setText(c.phone);
        holder.tvPoint.setText(String.valueOf(c.points));
        holder.tvNote.setText(c.note);

        holder.tvDateCreated.setText(c.createdDate);
        holder.tvDateUpdated.setText(c.updatedDate);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(c.phone, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhone, tvPoint, tvNote, tvDateCreated, tvDateUpdated;
        ImageView btnDelete;
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvPoint = itemView.findViewById(R.id.tvPoint);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvDateCreated = itemView.findViewById(R.id.tvDateCreated);
            tvDateUpdated = itemView.findViewById(R.id.tvDateUpdated);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

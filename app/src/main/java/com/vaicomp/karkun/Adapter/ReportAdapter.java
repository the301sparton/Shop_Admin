package com.vaicomp.karkun.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vaicomp.karkun.Modals.CartItem;
import com.vaicomp.karkun.R;
import com.vaicomp.karkun.db.ReportModal;

import java.text.MessageFormat;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<ReportModal> cartItems;

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, quantity, amount;

        ReportViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.itemName);
            quantity = view.findViewById(R.id.itemQuantity);
            amount = view.findViewById(R.id.itemRate);
        }
    }


    public ReportAdapter(List<ReportModal> moviesList) {
        this.cartItems = moviesList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_item, parent, false);

        return new ReportViewHolder(itemView);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(ReportViewHolder holder, final int position) {
        ReportModal item = cartItems.get(position);
        holder.itemName.setText(item.getName());
        holder.quantity.setText(MessageFormat.format("Quantity: {0}", item.getQuantity()));
        holder.amount.setText(MessageFormat.format("Amount: {0}", item.getAmount()));
    }


    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
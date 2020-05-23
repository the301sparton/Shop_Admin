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

import java.text.MessageFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private int type;
    private Activity context;

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, rateQ, amount;
        ImageView delteBtn;

        CartViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.itemName);
            rateQ = view.findViewById(R.id.rateQ);
            amount = view.findViewById(R.id.amount);
            delteBtn = view.findViewById(R.id.deleteItem);
        }
    }


    public CartAdapter(List<CartItem> moviesList, int type, Activity context) {
        this.cartItems = moviesList;
        this.context = context;
        this.type = type;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_list, parent, false);

        return new CartViewHolder(itemView);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {
        CartItem item = cartItems.get(position);
        holder.itemName.setText(item.getItemName());
        holder.rateQ.setText(MessageFormat.format("{0} X ₹ {1}", item.getQuantity(), item.getRate()));
        holder.amount.setText(String.format("₹ %s", item.getRate() * item.getQuantity()));

    }

    public List<CartItem> getAdapterData(){
        return  cartItems;
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
package com.vaicomp.karkun.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vaicomp.karkun.ItemViewActivity;
import com.vaicomp.karkun.R;
import com.vaicomp.karkun.db.ShopItem;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private List<ShopItem> itemList;
    private Activity context;

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView name;
        private TextView rate;
        private ImageView icon;
        private LinearLayout base;

        MyViewHolder(View convertView) {
            super(convertView);
            base = convertView.findViewById(R.id.baseView);
            base.setOnClickListener(this);
            name = convertView.findViewById(R.id.itemName);
            icon = convertView.findViewById(R.id.item_image);
            rate = convertView.findViewById(R.id.itemRate);
        }

        @Override
        public void onClick(View v) {
            viewItemFunction(getAdapterPosition());
        }

        private void viewItemFunction(final int pos) {
            context.startActivity(new Intent(context, ItemViewActivity.class)
                    .putExtra("ITEM_ID", itemList.get(pos).getItemId()));
        }
    }

    @SuppressLint("StaticFieldLeak")
    public ItemAdapter(List<ShopItem> itemList, Activity activity) {
        this.itemList = itemList;
        this.context = activity;
    }


    @NonNull
    @Override
    public ItemAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        ShopItem item = itemList.get(position);

        holder.name.setText(item.getItemName());
        holder.rate.setText("Rate: ₹" + item.getRate());
        Picasso.get()
                .load(item.getImageUrl())
                .resize(350, 350)
                .centerCrop()
                .into(holder.icon);
 }
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
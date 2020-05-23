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
import com.squareup.picasso.Picasso;
import com.vaicomp.karkun.R;
import com.vaicomp.karkun.db.ShopItem;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private List<ShopItem> itemList;


    static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView rate;
        private ImageView icon;

        MyViewHolder(View convertView) {
            super(convertView);
            name = convertView.findViewById(R.id.itemName);
            icon = convertView.findViewById(R.id.item_image);
            rate = convertView.findViewById(R.id.itemRate);
        }

    }

    @SuppressLint("StaticFieldLeak")
    public ItemAdapter(List<ShopItem> itemList, Activity activity) {
        this.itemList = itemList;
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
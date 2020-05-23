package com.vaicomp.karkun;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vaicomp.karkun.Adapter.CartAdapter;
import com.vaicomp.karkun.Modals.OrderModal;
import com.vaicomp.karkun.db.AppDataBase;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CartActivity extends AppCompatActivity {

    double deliveryCharge;
    CartAdapter adapter;
    AppDataBase db;
    FirebaseFirestore fdb;
    OrderModal omGlobal;
    Button placeOrderBtn;
    String order_id;
    ScrollView baseView;
    LinearLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cart);

        setTitle("Order Summary");


        baseView = findViewById(R.id.baseView);
        loader = findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);
        baseView.setVisibility(View.GONE);

        order_id = getIntent().getStringExtra("ORDER_ID");

        fdb = FirebaseFirestore.getInstance();
        placeOrderBtn = findViewById(R.id.placeOrderBtn);


        initViews(order_id);


        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrderBtn.setEnabled(false);
                final Context context = getApplicationContext();
                if (omGlobal.getState() == 2 || omGlobal.getState() == 1) {
                    fdb.collection("orders").document(order_id)
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            placeOrderBtn.setEnabled(true);
                            Toasty.success(context, "Order Canceled Successfully", Toasty.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else if (omGlobal.getState() == 3) {

                    fdb.collection("orders").document(order_id).update("state", 4).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toasty.success(getApplicationContext(), "Order Received", Toasty.LENGTH_SHORT).show();
                            placeOrderBtn.setText(getString(R.string.orderState4));
                            placeOrderBtn.setEnabled(false);
                            TextView tv = findViewById(R.id.orderState);
                            tv.setText(getString(R.string.orderState4));
                        }
                    });
                }

            }
        });


    }

    private void initViews(final String order_idT) {
        if (!order_idT.equals("NA")) {
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDataBase.class, "clientAppDB").fallbackToDestructiveMigration().build();
            fdb.collection("orders").document(order_idT).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    OrderModal orderModal = documentSnapshot.toObject(OrderModal.class);
                    omGlobal = orderModal;
                    order_id = documentSnapshot.getId();

                    RecyclerView categoryList = findViewById(R.id.list);
                    RecyclerView.LayoutManager mLayoutManager = new CustomGridLayoutManager(getApplicationContext());
                    categoryList.setLayoutManager(mLayoutManager);
                    categoryList.setItemAnimator(new DefaultItemAnimator());

                    adapter = new CartAdapter(orderModal.getItemList(), orderModal.getState(), CartActivity.this);
                    categoryList.setAdapter(adapter);


                    TextView tv = findViewById(R.id.itemTotal);
                    tv.setText(MessageFormat.format("₹ {0}", orderModal.getItemTotal()));

                    tv = findViewById(R.id.totalAmount);
                    tv.setText(String.format("₹ %s", orderModal.getItemTotal() + orderModal.getDeliveryCost()));

                    tv = findViewById(R.id.orderNumber);
                    tv.setText(order_id);

                    tv = findViewById(R.id.clientName);
                    tv.setText(omGlobal.getUname());

                    tv = findViewById(R.id.clientAddress);
                    tv.setText(omGlobal.getDeliveryAddress());

                    tv = findViewById(R.id.phoneNumberValue);
                    tv.setText(omGlobal.getPhoneNumber());

                    tv = findViewById(R.id.Date);
                    String pattern = "MMMM dd, yyyy hh:mm a";
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat(pattern, new Locale("en", "IN"));
                    tv.setText(simpleDateFormat.format(orderModal.getDate()));

                    tv = findViewById(R.id.orderState);
                    if (orderModal.getState() == 1) {
                        placeOrderBtn.setText("Cancel Order");
                        tv.setText(getString(R.string.orderState1));
                    } else if (orderModal.getState() == 2) {
                        placeOrderBtn.setText("Cancel Order");
                        tv.setText(getString(R.string.orderState2));
                    } else if (orderModal.getState() == 3) {
                        placeOrderBtn.setText("Confirm Delivery");
                        tv.setText(getString(R.string.orderState3));
                    } else if (orderModal.getState() == 4) {
                        placeOrderBtn.setText("Order Delivered.");
                        tv.setText(getString(R.string.orderState4));
                    }

                    baseView.setVisibility(View.VISIBLE);
                    loader.setVisibility(View.GONE);
                }
            });
        }

    }


    public static class CustomGridLayoutManager extends LinearLayoutManager {
        public CustomGridLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }
}

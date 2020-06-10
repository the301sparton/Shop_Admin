package com.vaicomp.karkun;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.vaicomp.karkun.ui.Reports.ReportFragment;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    Spinner slot;
    ArrayList<String> spinnerArray;

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
        slot = findViewById(R.id.slotValue);

        fdb = FirebaseFirestore.getInstance();
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        fdb.collection("shopDetails").document("/d1ajtkwauTOe8z27xdH8").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                spinnerArray = new ArrayList<>();
                spinnerArray = (ArrayList<String>) documentSnapshot.get("slots");
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                        (getApplicationContext(), android.R.layout.simple_spinner_item,
                                spinnerArray);
                slot.setAdapter(spinnerArrayAdapter);
                slot.setSelection(spinnerArray.size() - 1);
                initViews(order_id);
            }
        });


        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrderBtn.setEnabled(false);
                final Context context = getApplicationContext();
                if (omGlobal.getState() == 1) {
                    fdb.collection("orders").document(order_id).update("state", 2).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            placeOrderBtn.setEnabled(true);
                            initViews(order_id);
                            Toasty.success(context, "Order State Changed").show();
                        }
                    });
                } else if (omGlobal.getState() == 2) {
                    fdb.collection("orders").document(order_id).update("state", 3).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            placeOrderBtn.setEnabled(true);
                            initViews(order_id);
                            Toasty.success(context, "Order State Changed").show();
                        }
                    });
                } else if (omGlobal.getState() == 3) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + omGlobal.getPhoneNumber()));
                    startActivity(callIntent);
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
                    tv.setText(MessageFormat.format("₹ {0}", ReportFragment.round(orderModal.getItemTotal(), 2)));

                    tv = findViewById(R.id.totalAmount);
                    tv.setText(String.format("₹ %s", ReportFragment.round(orderModal.getItemTotal() + orderModal.getDeliveryCost(), 2)));

                    tv = findViewById(R.id.orderNumber);
                    tv.setText(order_id);

                    tv = findViewById(R.id.clientName);
                    tv.setText(omGlobal.getUname());

                    tv = findViewById(R.id.clientAddress);
                    tv.setText(omGlobal.getDeliveryAddress());

                    tv = findViewById(R.id.phoneNumberValue);
                    tv.setText(omGlobal.getPhoneNumber());

                    int position = 0, i = 0;
                    for(String slotName : spinnerArray){
                        if(slotName.equals(orderModal.getOrderSlot())){
                            position = i;
                            break;
                        }
                        i++;
                    }
                    slot.setSelection(position);
                    slot.setEnabled(false);

                    tv = findViewById(R.id.Date);
                    String pattern = "MMMM dd, yyyy hh:mm a";
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat(pattern, new Locale("en", "IN"));
                    tv.setText(simpleDateFormat.format(orderModal.getDate()));

                    tv = findViewById(R.id.orderState);
                    if (orderModal.getState() == 1) {
                        placeOrderBtn.setText("Accept Order");
                        tv.setText(getString(R.string.orderState1));
                    } else if (orderModal.getState() == 2) {
                        placeOrderBtn.setText("Mark Out For Delivery");
                        tv.setText(getString(R.string.orderState2));
                    } else if (orderModal.getState() == 3) {
                        placeOrderBtn.setText("Call Client");
                        tv.setText(getString(R.string.orderState3));
                    }

                    baseView.setVisibility(View.VISIBLE);
                    loader.setVisibility(View.GONE);
                }
            });
        }

    }


    public static class CustomGridLayoutManager extends LinearLayoutManager {
        CustomGridLayoutManager(Context context) {
            super(context);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }
    }
}

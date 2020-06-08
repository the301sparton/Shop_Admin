package com.vaicomp.karkun.ui.Reports;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaicomp.karkun.Adapter.HistoryAdapter;
import com.vaicomp.karkun.Adapter.ReportAdapter;
import com.vaicomp.karkun.Modals.CartItem;
import com.vaicomp.karkun.Modals.OrderModal;
import com.vaicomp.karkun.R;
import com.vaicomp.karkun.db.ReportModal;
import com.vaicomp.karkun.db.ShopItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ReportFragment extends Fragment {
    LinearLayout loader;
    private FirebaseFirestore db;
    private List<OrderModal> orderList;
    static EditText startDate;
    static EditText endDate;
    private RecyclerView listView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_reports, container, false);

        startDate = root.findViewById(R.id.startDate);
        endDate = root.findViewById(R.id.endDate);

        loader = root.findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);

        listView = root.findViewById(R.id.list);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = 5;
                outRect.right = 5;
                outRect.bottom = 40;

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildLayoutPosition(view) == 0) {
                    outRect.top = 5;
                } else {
                    outRect.top = 0;
                }
            }
        });

        final Spinner spinner = root.findViewById(R.id.reportTypePicker);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.reportTypes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        db = FirebaseFirestore.getInstance();
        db.collection("orders").whereGreaterThan("state", 0).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                orderList = new ArrayList<>();
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    OrderModal orderModal = querySnapshot.toObject(OrderModal.class);
                    orderModal.setOrderId(querySnapshot.getId());
                    orderList.add(orderModal);
                }


                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTruitonDatePickerDialog(view);
                    }
                });
                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showToDatePickerDialog(view);
                    }
                });

                Button goBtn = root.findViewById(R.id.getReportBtn);
                goBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<ReportModal> filteredList = new ArrayList<>();
                        List<String> unames = new ArrayList<>();
                        if(spinner.getSelectedItem().toString().equals("User Report")){
                            // Process for User Report
                            for(OrderModal modal : orderList){
                              if(!unames.contains(modal.getUname())){
                                  unames.add(modal.getUname());
                              }
                            }

                            for(String user : unames){
                                int count = 0;
                                Double userAmount = 0.0;
                                for(OrderModal modal : orderList){
                                    if(user.equals(modal.getUname())){
                                        userAmount += modal.getGrandTotal();
                                        count++;
                                    }
                                }
                                ReportModal reportModal = new ReportModal();
                                reportModal.setName(user);
                                reportModal.setAmount(round(userAmount,2));
                                reportModal.setQuantity(count);
                                filteredList.add(reportModal);
                            }

                        }
                        else{
                            // Process for ITEM Report

                            for(OrderModal modal : orderList){
                               for(CartItem item : modal.getItemList()){
                                   if(!unames.contains(item.getItemName())){
                                       unames.add(item.getItemName());
                                   }
                               }
                            }

                            for(String user : unames){
                                int count = 0;
                                Double userAmount = 0.0;
                                for(OrderModal modal : orderList){
                                    for(CartItem cartItem : modal.getItemList()){
                                        if(user.equals(cartItem.getItemName())){
                                            count += cartItem.getQuantity();
                                            userAmount += cartItem.getAmount();
                                        }
                                    }
                                }

                                ReportModal reportModal = new ReportModal();
                                reportModal.setName(user);
                                reportModal.setAmount(userAmount);
                                reportModal.setQuantity(count);
                                filteredList.add(reportModal);
                            }
                        }

                        Collections.sort(filteredList, new Comparator<ReportModal>() {
                            @Override
                            public int compare(ReportModal o1, ReportModal o2) {
                                return o2.getAmount().compareTo(o1.getAmount());
                            }
                        });

                        // We have the processed list here
                        ReportAdapter adapter = new ReportAdapter(filteredList);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                        listView.setLayoutManager(mLayoutManager);

                        listView.setItemAnimator(new DefaultItemAnimator());
                        listView.setAdapter(adapter);
                    }
                });

                loader.setVisibility(View.GONE);
            }
        });
        return root;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    public void showToDatePickerDialog(View v) {
        DialogFragment newFragment = new ToDatePickerFragment();
        newFragment.show(getParentFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog;
            datePickerDialog = new DatePickerDialog(getActivity(),this, year,
                    month,day);
            return datePickerDialog;
        }


        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            startDate.setText(day + "/" + month  + "/" + year);
        }

    }

    public static class ToDatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        // Calendar startDateCalendar=Calendar.getInstance();
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            String getfromdate = startDate.getText().toString().trim();
            Date date = new Date();
            int year = date.getYear(),month = date.getMonth(),day = date.getDay();
            if(!getfromdate.equals("")){
                String getfrom[] = getfromdate.split("/");
                year= Integer.parseInt(getfrom[2]);
                month = Integer.parseInt(getfrom[1]);
                day = Integer.parseInt(getfrom[0]);

            }
            final Calendar c = Calendar.getInstance();
            c.set(year,month,day+1);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),this, year,month,day);
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            return datePickerDialog;
        }
        public void onDateSet(DatePicker view, int year, int month, int day) {

            endDate.setText(day + "/" + month  + "/" + year);
        }
    }
}

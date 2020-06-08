package com.vaicomp.karkun.ui.Reports;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaicomp.karkun.Modals.OrderModal;
import com.vaicomp.karkun.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationsFragment extends Fragment {
    LinearLayout loader;
    private FirebaseFirestore db;
    private List<OrderModal> orderList;
    static EditText startDate;
    static EditText endDate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_reports, container, false);

        startDate = root.findViewById(R.id.startDate);
        endDate = root.findViewById(R.id.endDate);

        loader = root.findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);

        Spinner spinner = root.findViewById(R.id.reportTypePicker);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.reportTypes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        db = FirebaseFirestore.getInstance();
        db.collection("orders").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

                loader.setVisibility(View.GONE);
            }
        });
        return root;
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

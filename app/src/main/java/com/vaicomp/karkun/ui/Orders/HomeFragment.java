package com.vaicomp.karkun.ui.Orders;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaicomp.karkun.Adapter.HistoryAdapter;
import com.vaicomp.karkun.Modals.OrderModal;
import com.vaicomp.karkun.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView listView;
    private List<OrderModal> list;
    private TextView sortingByText;
    private LinearLayout loader;
    private HistoryAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order, container, false);
        list = new ArrayList<>();

        listView = root.findViewById(R.id.list);

        loader = root.findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        sortingByText  = root.findViewById(R.id.sortingByText);

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





        Spinner spinner = root.findViewById(R.id.sortType);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = String.valueOf(parent.getItemAtPosition(position));
                switch (type) {
                    case "Order Time":
                        sortingByText.setText("Sorted By : Order Time");
                        Collections.sort(list, new Comparator<OrderModal>() {
                            @Override
                            public int compare(OrderModal o1, OrderModal o2) {
                                return o2.getDate().compareTo(o1.getDate());
                            }
                        });
                        break;
                    case "Order Status":
                        sortingByText.setText("Sorted By : Order State");
                        List<OrderModal> l1 = new ArrayList<>();
                        l1.addAll(list);
                        Collections.sort(l1, new Comparator<OrderModal>() {
                            @Override
                            public int compare(OrderModal o1, OrderModal o2) {
                                return String.valueOf(o1.getState()).compareTo(String.valueOf(o2.getState()));
                            }
                        });
                        list.clear();
                        for( OrderModal o : l1){
                            if(o.getState() > 0){
                                list.add(o);
                            }
                        }
                        for( OrderModal o : l1){
                            if(o.getState() == 0){
                                list.add(o);
                            }
                        }
                        break;
                    case "User":
                        sortingByText.setText("Sorted By : User");
                        Collections.sort(list, new Comparator<OrderModal>() {
                            @Override
                            public int compare(OrderModal o1, OrderModal o2) {
                                return o2.getUid().compareTo(o1.getUid());
                            }
                        });
                        break;
                }
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
                else {
                    adapter = new HistoryAdapter(list, getActivity());
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                    listView.setLayoutManager(mLayoutManager);

                    listView.setItemAnimator(new DefaultItemAnimator());
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("orders").whereLessThanOrEqualTo("state", 4).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                list.clear();
                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                    OrderModal orderModal = querySnapshot.toObject(OrderModal.class);
                    orderModal.setOrderId(querySnapshot.getId());
                    list.add(orderModal);
                    Log.i("OBJ: ", String.valueOf(orderModal));
                }

                if (queryDocumentSnapshots.size() > 0) {

                    Collections.sort(list, new Comparator<OrderModal>() {
                        @Override
                        public int compare(OrderModal o1, OrderModal o2) {
                            return o2.getDate().compareTo(o1.getDate());
                        }
                    });

                    if (adapter == null) {
                        adapter = new HistoryAdapter(list, getActivity());
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                        listView.setLayoutManager(mLayoutManager);

                        listView.setItemAnimator(new DefaultItemAnimator());
                        listView.setAdapter(adapter);
                    }
                    else {
                        adapter.notifyDataSetChanged();
                    }
                }

                listView.setVisibility(View.VISIBLE);
                loader.setVisibility(View.GONE);
            }
        });
    }

}

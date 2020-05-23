package com.vaicomp.karkun.ui.Items;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaicomp.karkun.Adapter.ItemAdapter;
import com.vaicomp.karkun.R;
import com.vaicomp.karkun.db.AppDataBase;
import com.vaicomp.karkun.db.ShopItem;
import com.vaicomp.karkun.preferenceManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView listView;
    private List<ShopItem> list, searchList, fireBaseList;
    private ItemAdapter adapter;
    private TextInputEditText searchBar;
    private Button clear;
    private LinearLayout loader;

    private Context context;
    private AppDataBase local_db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("StaticFieldLeak")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        View root = inflater.inflate(R.layout.fragment_items, container, false);

        listView = root.findViewById(R.id.list);
        loader = root.findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);

        listView.setVisibility(View.GONE);

        local_db = Room.databaseBuilder(requireActivity(),
                AppDataBase.class, "clientAppDB").fallbackToDestructiveMigration().build();
        context = requireContext();
        searchBar = root.findViewById(R.id.searchBar);
        clear = root.findViewById(R.id.calc_clear_txt_Prise);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
            }
        });


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().equals("")) {
                    clear.setVisibility(View.VISIBLE);
                    searchList = new ArrayList<>();
                    for (ShopItem item : fireBaseList) {
                        if (item.getItemName().toLowerCase().matches(s.toString().toLowerCase() + ".*")) {
                            searchList.add(item);
                        }
                    }
                    list.clear();
                    list.addAll(searchList);
                } else {
                    clear.setVisibility(View.GONE);
                    list.clear();
                    list.addAll(fireBaseList);
                }
                adapter.notifyDataSetChanged();
            }
        });


        return root;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onResume() {
        super.onResume();


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final String itemState = String.valueOf(documentSnapshot.get("itemState"));
                if (!itemState.equals(preferenceManager.getItemState(context))) {
                    db.collection("shopItems").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> list1 = queryDocumentSnapshots.getDocuments();
                            final ShopItem[] itemList = new ShopItem[list1.size()];
                            int itr = 0;
                            for (DocumentSnapshot ds : list1) {
                                ShopItem item = new ShopItem();
                                item.setItemId(ds.getId());
                                item.setItemName(String.valueOf(ds.get("itemName")));
                                item.setCategory(String.valueOf(ds.get("category")));
                                item.setImageUrl(String.valueOf(ds.get("photoUrl")));
                                item.setRate(Double.valueOf(String.valueOf(ds.get("itemRate"))));
                                item.setAmount((double) 0);
                                item.setQuantity(0);

                                itemList[itr] = item;
                                itr++;
                            }
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    local_db.shopItemDao().nukeTable();
                                    local_db.shopItemDao().insertAll(itemList);
                                    fireBaseList = local_db.shopItemDao().getAll();
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    if (adapter == null) {
                                        list = new ArrayList<>();
                                        list.addAll(fireBaseList);
                                        adapter = new ItemAdapter(list, getActivity());
                                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                                        listView.setLayoutManager(mLayoutManager);
                                        listView.setItemAnimator(new DefaultItemAnimator());
                                        listView.setAdapter(adapter);
                                    } else {
                                        list.clear();
                                        list.addAll(fireBaseList);
                                        if (list.size() > 0) {
                                            listView.setVisibility(View.VISIBLE);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            listView.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    preferenceManager.setItemState(context, itemState);
                                    listView.setVisibility(View.VISIBLE);
                                    loader.setVisibility(View.GONE);
                                }
                            }.execute();
                        }
                    });
                } else {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            fireBaseList = local_db.shopItemDao().getAll();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if (adapter == null) {
                                list = new ArrayList<>();
                                list.addAll(fireBaseList);
                                adapter = new ItemAdapter(list, getActivity());
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                                listView.setLayoutManager(mLayoutManager);
                                listView.setItemAnimator(new DefaultItemAnimator());
                                listView.setAdapter(adapter);
                            } else {
                                list.clear();
                                list.addAll(fireBaseList);
                                if (list.size() > 0) {
                                    listView.setVisibility(View.VISIBLE);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    listView.setVisibility(View.INVISIBLE);
                                }
                            }
                            preferenceManager.setItemState(context, itemState);
                            listView.setVisibility(View.VISIBLE);
                            loader.setVisibility(View.GONE);
                        }
                    }.execute();
                }
            }
        });
    }
}


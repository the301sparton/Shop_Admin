package com.vaicomp.karkun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vaicomp.karkun.Adapter.CategoryGridAdapter;
import com.vaicomp.karkun.db.AppDataBase;
import com.vaicomp.karkun.db.CategoryFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import es.dmoral.toasty.Toasty;

public class CreateCategoryActivity extends AppCompatActivity {
    private CategoryGridAdapter adapter;
    private List<CategoryFilter> categoryFilterList;
    private FirebaseFirestore db;
    private LinearLayout loader;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_category);
        loader = findViewById(R.id.loader);
        db = FirebaseFirestore.getInstance();

             db.collection("category").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> list1 = queryDocumentSnapshots.getDocuments();
                    categoryFilterList = new ArrayList<>();
                    for(DocumentSnapshot ds : list1){
                        categoryFilterList.add( new CategoryFilter(String.valueOf(ds.get("categoryName")),false));
                    }
                    Collections.sort(categoryFilterList, new Comparator<CategoryFilter>() {
                        @Override
                        public int compare(CategoryFilter o1, CategoryFilter o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

                    RecyclerView gridView = findViewById(R.id.grid_view);
                    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    gridView.setLayoutManager(mLayoutManager);
                    gridView.setItemAnimator(new DefaultItemAnimator());

                    gridView.setHasFixedSize(false);
                    adapter = new CategoryGridAdapter(CreateCategoryActivity.this, categoryFilterList);
                    gridView.setAdapter(adapter);
                    loader.setVisibility(View.GONE);
                }
            });




        final EditText newCategory = findViewById(R.id.newCategoryName);
        Button newCategoryCreateBtn = findViewById(R.id.create_category_btn);

        newCategoryCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newCategory.getText().toString().equals("")){
                     loader.setVisibility(View.VISIBLE);
                    Map<String, String> temp = new HashMap<>();
                    temp.put("categoryName", newCategory.getText().toString());
                    db.collection("category").add(temp).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8")
                                    .update("itemState", String.valueOf(new Date())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toasty.success(getApplicationContext(), "Category Created Successfully",Toasty.LENGTH_SHORT).show();
                                    loader.setVisibility(View.GONE);
                                    finish();
                                }
                            });
                        }
                    });
                }else{
                    Toasty.error(getApplicationContext(), "Please Enter Category Name", Toasty.LENGTH_SHORT).show();
                }
            }
        });
    }
}
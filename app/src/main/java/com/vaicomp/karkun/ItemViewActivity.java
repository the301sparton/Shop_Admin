package com.vaicomp.karkun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vaicomp.karkun.db.ShopItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class ItemViewActivity extends AppCompatActivity {
    private Button btnUpload;
    private RelativeLayout content;
    private LinearLayout loader;
    private TextView loaderText;
    private ImageView imageView;
    private Spinner categoryPicker;
    private String itemCat;
    private String item_id;
    private FirebaseFirestore db;
    private final int PICK_IMAGE_REQUEST = 71;
    TextInputEditText itemNameView, itemRateView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_item_view);

        item_id = getIntent().getStringExtra("ITEM_ID");
        imageView = findViewById(R.id.ic_shop);
        loader = findViewById(R.id.loader);
        content = findViewById(R.id.content);
        categoryPicker = findViewById(R.id.categoryPicker);
        loaderText = findViewById(R.id.loaerText);
        loaderText.setText("Loading..");

        itemNameView = findViewById(R.id.itemName);
        itemRateView = findViewById(R.id.itemRate);

        btnUpload = findViewById(R.id.saveItem);

        content.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        db = FirebaseFirestore.getInstance();
        if(!item_id.equals("NA")) {
                db.collection("shopItems").document(item_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        Picasso.get()
                                .load(String.valueOf(documentSnapshot.get("photoUrl")))
                                .resize(350, 350)
                                .centerCrop()
                                .into(imageView);

                        TextInputEditText inputEditText = findViewById(R.id.itemName);
                        inputEditText.setText(String.valueOf(documentSnapshot.get("itemName")));

                        inputEditText = findViewById(R.id.itemRate);
                        inputEditText.setText(String.valueOf(documentSnapshot.get("itemRate")));

                        itemCat = String.valueOf(documentSnapshot.get("category"));

                        db.collection("category").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                int selectPosition = queryDocumentSnapshots.size(), i = 0;
                                ArrayList<String> spinnerArray = new ArrayList<>();
                                for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                    spinnerArray.add(String.valueOf(documentSnapshot1.get("categoryName")));
                                    if (itemCat.equals(String.valueOf(documentSnapshot1.get("categoryName")))) {
                                        selectPosition = i;
                                    }
                                    i++;
                                }
                                spinnerArray.add("NA");
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                        (getApplicationContext(), android.R.layout.simple_spinner_item,
                                                spinnerArray);
                                categoryPicker.setAdapter(spinnerArrayAdapter);
                                categoryPicker.setSelection(selectPosition);

                                loader.setVisibility(View.GONE);
                                content.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
            else{
                db.collection("category").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int selectPosition = queryDocumentSnapshots.size();
                        ArrayList<String> spinnerArray = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                            spinnerArray.add(String.valueOf(documentSnapshot1.get("categoryName")));
                        }
                        spinnerArray.add("NA");
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                (getApplicationContext(), android.R.layout.simple_spinner_item,
                                        spinnerArray);
                        categoryPicker.setAdapter(spinnerArrayAdapter);
                        categoryPicker.setSelection(selectPosition);

                        loader.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                    }
                });
            }



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!itemNameView.getText().toString().equals("")  &&  !itemRateView.getText().toString().equals("")) {
                    loader.setVisibility(View.VISIBLE);
                    content.setVisibility(View.GONE);
                    loaderText.setText("Creating Item..");
                    btnUpload.setEnabled(false);
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("itemName", itemNameView.getText().toString());
                    item.put("itemRate", Double.valueOf(itemRateView.getText().toString()));
                    item.put("category", String.valueOf(categoryPicker.getSelectedItem()));


                    if (item_id.equals("NA")) {
                        db.collection("shopItems").add(item).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(final DocumentReference documentReference) {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                final StorageReference mountainsRef = storageRef.child(documentReference.getId() + ".jpg");
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();
                                UploadTask uploadTask = mountainsRef.putBytes(data);

                                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return mountainsRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            db.collection("shopItems").document(documentReference.getId()).update("photoUrl", String.valueOf(downloadUri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8")
                                                            .update("itemState", String.valueOf(new Date())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            loader.setVisibility(View.GONE);
                                                            content.setVisibility(View.VISIBLE);
                                                            Toasty.success(getApplicationContext(), "Item Created Successfully",Toasty.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            Toasty.error(getApplicationContext(), "Item Creation Failed", Toasty.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        loaderText.setText("Upload is " + progress + "% done");
                                    }
                                });
                            }
                        });
                    } else {
                        db.collection("shopItems").document(item_id).set(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                final StorageReference mountainsRef = storageRef.child(item_id + ".jpg");
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();
                                UploadTask uploadTask = mountainsRef.putBytes(data);
                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return mountainsRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            db.collection("shopItems").document(item_id).update("photoUrl", String.valueOf(downloadUri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8")
                                                            .update("itemState", String.valueOf(new Date())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            loader.setVisibility(View.GONE);
                                                            content.setVisibility(View.VISIBLE);
                                                            Toasty.success(getApplicationContext(), "Item Updated Successfully",Toasty.LENGTH_SHORT).show();
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            Toasty.error(getApplicationContext(), "Item Updating Failed", Toasty.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                        System.out.println("Upload is " + progress + "% done");
                                        int currentprogress = (int) progress;
                                        loaderText.setText("Upload is " + currentprogress + "% done");
                                    }
                                });
                            }
                        });
                    }
                }
                else {

                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Picasso.get()
                    .load(filePath)
                    .resize(350, 350)
                    .centerCrop()
                    .into(imageView);
        }
    }

}

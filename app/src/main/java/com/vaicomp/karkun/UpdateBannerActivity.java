package com.vaicomp.karkun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class UpdateBannerActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    private ImageView imageView;
    private Button btnUpload;
    private RelativeLayout content;
    private LinearLayout loader;
    private TextView loaderText;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_update_banner);
        loader = findViewById(R.id.loader);
        content = findViewById(R.id.content);
        loaderText = findViewById(R.id.loaerText);
        loaderText.setText("Loading..");
        imageView = findViewById(R.id.ic_shop);

        db = FirebaseFirestore.getInstance();

        db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String bannerUrl = String.valueOf(documentSnapshot.get("bannerURL"));
                Picasso.get()
                        .load(bannerUrl)
                        .fit()
                        .into(imageView);
                loader.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
            }
        });



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        btnUpload = findViewById(R.id.saveItem);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
                loaderText.setText("Updating Banner..");
                btnUpload.setEnabled(false);

                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                final StorageReference mountainsRef = storageRef.child("shopImage" + ".jpg");
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
                            db.collection("shopItemState").document("d1ajtkwauTOe8z27xdH8").update("bannerURL", String.valueOf(downloadUri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    loader.setVisibility(View.GONE);
                                    content.setVisibility(View.VISIBLE);
                                    Toasty.success(getApplicationContext(), "Banner Updated Successfully",Toasty.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        } else {
                            Toasty.error(getApplicationContext(), "Banner Updating Failed", Toasty.LENGTH_SHORT).show();
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

         //load previous data





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Picasso.get()
                    .load(filePath)
                    .fit()
                    .into(imageView);
        }
    }
}
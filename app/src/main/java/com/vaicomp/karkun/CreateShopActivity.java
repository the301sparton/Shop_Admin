package com.vaicomp.karkun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class CreateShopActivity extends AppCompatActivity {
    boolean shopIdVerified = false;
    TextInputEditText shopId, password, newPassword, shopName;
    SmoothProgressBar progress_horizontal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_shop);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        password = findViewById(R.id.password);
        newPassword = findViewById(R.id.passwordC);
        shopName = findViewById(R.id.shopName);

        progress_horizontal = findViewById(R.id.progress_horizontal);

        shopId = findViewById(R.id.shopId);
        // Shop Id Validation
        shopId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) {
                        if(!shopId.getText().toString().equals("")) {
                            progress_horizontal.setVisibility(View.VISIBLE);
                            db.collection("shops").whereEqualTo("shopId", shopId.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("res", String.valueOf(task.getResult().size()));
                                        if (Objects.requireNonNull(task.getResult()).size() != 0) {
                                            shopIdVerified = false;
                                            Drawable dr = getDrawable(R.drawable.ic_error);
                                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                                            shopId.setCompoundDrawables(null, null, dr, null);
                                        } else {
                                            shopIdVerified = true;
                                            Drawable dr = getDrawable(R.drawable.ic_tick);
                                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                                            shopId.setCompoundDrawables(null, null, dr, null);
                                        }
                                        progress_horizontal.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            shopIdVerified = false;
                            Drawable dr = getDrawable(R.drawable.ic_error);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            shopId.setCompoundDrawables(null, null, dr, null);
                        }
                    }
            }
        });

        Button createShop = findViewById(R.id.createShop);
        createShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = true;
                if (password.getText().toString().equals("")) {
                    password.setError("Password can not be empty");
                    flag = false;
                }

                if (newPassword.getText().toString().equals("")) {
                    newPassword.setError("Password can not be empty");
                    flag = false;
                }

                if (shopName.getText().toString().equals("")) {
                    shopName.setError("Shop Name can not be empty");
                    flag = false;
                }

                if (password.getText().toString().length() < 8) {
                    password.setError("Password length must be at lest 8 character");
                    flag = false;
                }

                if (!newPassword.getText().toString().equals(password.getText().toString())) {
                    newPassword.setError("Passwords must be same");
                    flag = false;
                }

                if(flag && shopIdVerified){
                    progress_horizontal.setVisibility(View.VISIBLE);
                    Map<String, Object> user = new HashMap<>();
                    user.put("shopId", shopId.getText().toString());
                    user.put("password", password.getText().toString());

                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(new Date());
                    user.put("createdOn", cal.getTime());
                    cal.add(Calendar.DAY_OF_YEAR, 30);
                    Date expireDate = cal.getTime();

                    user.put("expireOn",expireDate);
                    user.put("shopName",shopName.getText().toString());

                    TextInputEditText textInputEditText = findViewById(R.id.emailId);
                    user.put("emailId", textInputEditText.getText().toString());
                    textInputEditText = findViewById(R.id.phoneNumber);
                    user.put("phoneNumber", textInputEditText.getText().toString());
                    textInputEditText = findViewById(R.id.gstn);
                    user.put("gstn", textInputEditText.getText().toString());
                    textInputEditText = findViewById(R.id.istn);
                    user.put("istn",textInputEditText.getText().toString());
                    textInputEditText = findViewById(R.id.pan);
                    user.put("pan",textInputEditText.getText().toString());
                    textInputEditText = findViewById(R.id.address);
                    user.put("address", textInputEditText.getText().toString());

                    db.collection("shops").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toasty.success(getApplicationContext(),"Shop Created!",Toasty.LENGTH_LONG).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress_horizontal.setVisibility(View.GONE);
                            Toasty.error(getApplicationContext(),"Failed To Create Shop!",Toasty.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }
}

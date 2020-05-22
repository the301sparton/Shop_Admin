package com.vaicomp.karkun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);


        try {
            String vName = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView tv = findViewById(R.id.versionText);
            tv.setText(vName);

            if (!preferenceManager.getIsLoggedIn(getApplicationContext())) {
                SignInButton signInButton = findViewById(R.id.sign_in_button);
                TextView loader = findViewById(R.id.loader);
                loader.setVisibility(View.GONE);
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setSize(SignInButton.SIZE_STANDARD);

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();


                mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mAuth = FirebaseAuth.getInstance();
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, 200);
                    }
                });
            }
            else{
                SignInButton signInButton = findViewById(R.id.sign_in_button);
                signInButton.setVisibility(View.GONE);
                TextView loader = findViewById(R.id.loader);
                loader.setVisibility(View.VISIBLE);
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
                //loadAllDataToLocalDB(SplashActivity.this);
            }


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                updateUI(null);
                            }
                        }
                    });
        } catch (ApiException e) {
            Log.w("SIGNIN OP ==>> ", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }


    void updateUI(final FirebaseUser account){
        if(account != null){
            final Context context = getApplicationContext();
            preferenceManager.setUID(context, account.getUid());
            preferenceManager.setDisplayName(context, account.getDisplayName());
            preferenceManager.setEmailId(context, account.getEmail());
            preferenceManager.setPhotoUrl(context, String.valueOf(account.getPhotoUrl()));

            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("shopDetails").document("d1ajtkwauTOe8z27xdH8").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    boolean flag = false;
                    List<String> group = (List<String>) documentSnapshot.getData().get("accessList");
                    for (String key: group) {
                      if(account.getEmail().equals(key)){
                          flag = true;
                          break;
                      }
                    }
                    if(flag){
                        preferenceManager.setIsLoggedIn(context, true);
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        finish();
                    }
                    else{
                        Toasty.error(context,"You don't have access",Toasty.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else{
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            preferences.edit().clear().apply();
            Toasty.error(getApplicationContext(), "Sign-In Failed :(", Toasty.LENGTH_LONG).show();
        }
    }


}

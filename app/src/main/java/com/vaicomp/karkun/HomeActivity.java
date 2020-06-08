package com.vaicomp.karkun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vaicomp.karkun.db.AppDataBase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
       AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.logOut){
            LogOut(HomeActivity.this);
        }
        else if(item.getItemId() == R.id.upload_banner){
            startActivity(new Intent(HomeActivity.this, UpdateBannerActivity.class));
        }
        else if(item.getItemId() == R.id.create_category){
            startActivity(new Intent(HomeActivity.this, CreateCategoryActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    public static void LogOut(final Activity context){
        new Handler().postDelayed(new Runnable() {

            @Override
            @SuppressLint("StaticFieldLeak")
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                preferences.edit().clear().apply();
                AppDataBase db = Room.databaseBuilder(context,
                        AppDataBase.class, "clientAppDB").fallbackToDestructiveMigration().build();

                db.categoryFilterDao().nukeTable();
                db.shopItemDao().nukeTable();
                context.startActivity(new Intent(context, SplashActivity.class));
                context.finish();
            }
        }, 500);

    }
}

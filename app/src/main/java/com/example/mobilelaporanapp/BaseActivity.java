package com.example.mobilelaporanapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    private MaterialToolbar appBar;
    private ImageButton btnBack;
    private TextView tvTime;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBar = findViewById(R.id.app_bar);
        btnBack = findViewById(R.id.btn_back);
        tvTime = findViewById(R.id.tv_time);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        tvTime.setText("15:00");

        // Set warna tombol back ke purple_500
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));

        // Aksi ketika tombol back diklik
        btnBack.setOnClickListener(v -> onBackPressed());

        // Tampilkan fragment awal (Dashboard) jika activity baru dimulai
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), false); // tidak masuk ke backstack
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // Bottom navigation item click listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new DashboardFragment();
            } else if (item.getItemId() == R.id.nav_report) {
                selectedFragment = new ReportListFragment();
            } else if (item.getItemId() == R.id.nav_create) {
                selectedFragment = new ReportFragment();
            } else if (item.getItemId() == R.id.nav_location) {
                selectedFragment = new ReportUserFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                boolean addToBackStack = !(selectedFragment instanceof DashboardFragment);
                loadFragment(selectedFragment, addToBackStack);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        // Warna tombol back tetap purple_500
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));

        // Sembunyikan tombol back hanya di halaman Dashboard
        btnBack.setVisibility(fragment instanceof DashboardFragment ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}

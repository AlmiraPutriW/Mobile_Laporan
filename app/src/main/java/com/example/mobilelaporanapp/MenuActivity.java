package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout menuCreate, menuLaporan, menuAbout, menuKeunggulan, menuProfile, menuInfo;
    private Button btnLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // ganti sesuai nama layout

        // Bind view
        menuCreate = findViewById(R.id.menu_create);
        menuLaporan = findViewById(R.id.Laporan);
        menuInfo = findViewById(R.id.menu_info);
        menuKeunggulan = findViewById(R.id.menu_keunggulan);
        menuAbout = findViewById(R.id.menu_about);
        menuProfile = findViewById(R.id.menu_profile);
        btnLogout = findViewById(R.id.btnLogout);

        // Navigasi menu
        menuCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ReportFragment.class);
            startActivity(intent);
        });

        menuLaporan.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ReportListActivity.class);
            startActivity(intent);
        });

        menuInfo.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        menuAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        menuKeunggulan.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, FeaturesActivity.class);
            startActivity(intent);
        });

        menuProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutFromServer());
    }

    private void logoutFromServer() {
        // Ambil token dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);

        if (token == null) {
            logoutAndGoToDashboard();
            return;
        }

        String url = "https://backend-sipraja.vercel.app/api/v1/user/logout";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // Hapus token dan lanjut ke dashboard
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("token");
                    editor.apply();
                    logoutAndGoToDashboard();
                },
                error -> {
                    // Tetap logout lokal meski request gagal
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("token");
                    editor.apply();
                    logoutAndGoToDashboard();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void logoutAndGoToDashboard() {
        Intent intent = new Intent(MenuActivity.this, HomeFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

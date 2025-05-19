package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class DashboardActivity extends AppCompatActivity {

    private MaterialButton reportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Mulai service audio saat activity muncul
        Intent audioServiceIntent = new Intent(this, AudioService.class);
        startService(audioServiceIntent);

        reportButton = findViewById(R.id.report_button);

        reportButton.setOnClickListener(v -> {
            // Hapus token agar dianggap logout
            SharedPreferences prefs = getSharedPreferences("MY_APP", MODE_PRIVATE);
            prefs.edit().remove("TOKEN").apply();

            // Arahkan ke LoginActivity
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // tutup Dashboard agar tidak bisa kembali dengan tombol back
        });
    }
}

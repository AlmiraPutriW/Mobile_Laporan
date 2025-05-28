package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek token login di SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MY_APP", MODE_PRIVATE);
        String token = prefs.getString("TOKEN", null);

        if (token == null) {
            // Jika belum login, arahkan ke LoginActivity
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        } else {
            // Jika sudah login, arahkan ke DashboardActivity
            Intent intent = new Intent(this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Baris di bawah ini tidak akan pernah dieksekusi karena ada return di atas
        // Tapi kalau ingin menampilkan splash layout sebentar, letakkan di sini
        // setContentView(R.layout.activity_main);
    }
}

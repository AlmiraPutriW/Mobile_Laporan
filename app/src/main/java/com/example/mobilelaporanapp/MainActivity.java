package com.example.mobilelaporanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) return;

            switch (action) {
                case Intent.ACTION_BATTERY_CHANGED:
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    int batteryPct = (int) ((level / (float) scale) * 100);

                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    if (isCharging) {
                        Toast.makeText(context, "Sedang diisi daya. Baterai: " + batteryPct + "%", Toast.LENGTH_LONG).show();
                    } else if (batteryPct <= 20) {
                        Toast.makeText(context, "Baterai hanya " + batteryPct + "%. Segera isi daya!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Baterai: " + batteryPct + "%", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case ConnectivityManager.CONNECTIVITY_ACTION:
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                    if (activeNetwork != null && activeNetwork.isConnected()) {
                        int type = activeNetwork.getType();
                        String connectionType;
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            connectionType = "Terhubung dengan WiFi";
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            connectionType = "Terhubung dengan Data Seluler";
                        } else {
                            connectionType = "Terhubung dengan jaringan lain";
                        }
                        Toast.makeText(context, connectionType, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Opsional, bisa buat splash screen

        // Daftarkan receiver untuk monitoring baterai dan koneksi jaringan
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(statusReceiver, filter);

        // Cek token login di SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MY_APP", MODE_PRIVATE);
        String token = prefs.getString("TOKEN", null);

        if (token == null) {
            // Belum login, arahkan ke LoginActivity
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Sudah login, arahkan ke MenuActivity (atau Dashboard)
            Intent intent = new Intent(MainActivity.this, BaseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Jangan lupa unregister receiver agar tidak memory leak
        unregisterReceiver(statusReceiver);
    }
}

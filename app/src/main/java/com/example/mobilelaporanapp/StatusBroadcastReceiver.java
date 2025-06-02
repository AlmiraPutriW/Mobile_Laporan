package com.example.mobilelaporanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) return;

        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                ConnectivityManager cm = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    Toast.makeText(context, "Terhubung ke Internet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Tidak ada koneksi Internet", Toast.LENGTH_SHORT).show();
                }
                break;

            case Intent.ACTION_BATTERY_CHANGED:
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) ((level / (float) scale) * 100);
                Toast.makeText(context, "Baterai: " + batteryPct + "%", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

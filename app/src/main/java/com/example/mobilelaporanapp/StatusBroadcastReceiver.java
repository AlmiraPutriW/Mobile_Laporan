package com.example.mobilelaporanapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import com.google.android.material.appbar.MaterialToolbar;

public class StatusBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "StatusBroadcastReceiver";

    private final MaterialToolbar toolbar;

    public StatusBroadcastReceiver(Context context, MaterialToolbar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Received null intent or action");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION:
                boolean connected = isConnected(context);
                Log.d(TAG, "Connectivity status: " + (connected ? "Connected" : "Disconnected"));
                toolbar.setSubtitle(connected ? "Online" : "Offline");
                break;

            case Intent.ACTION_BATTERY_CHANGED:
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                Log.d(TAG, "Battery level raw: " + level + ", scale: " + scale);

                if (level >= 0 && scale > 0) {
                    int batteryPct = (int) ((level / (float) scale) * 100);
                    Log.d(TAG, "Battery percentage calculated: " + batteryPct + "%");
                    toolbar.setTitle("Baterai: " + batteryPct + "%");
                } else {
                    Log.w(TAG, "Invalid battery level or scale");
                }
                break;

            default:
                Log.d(TAG, "Unhandled action: " + action);
                break;
        }
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            Log.w(TAG, "ConnectivityManager is null");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            boolean isConnected = capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            Log.d(TAG, "NetworkCapabilities check: " + (isConnected ? "Connected" : "Disconnected"));
            return isConnected;
        } else {
            android.net.NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnected();
            Log.d(TAG, "NetworkInfo check: " + (isConnected ? "Connected" : "Disconnected"));
            return isConnected;
        }
    }
}

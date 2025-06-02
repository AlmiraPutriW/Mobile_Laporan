package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class DashboardActivity extends AppCompatActivity {

    private MaterialButton reportButton;
    private Handler textAnimHandler;
    private Runnable textAnimRunnable;

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
            finish();
        });

        // Animasi teks per huruf looping untuk TextView hero_text
        TextView heroText = findViewById(R.id.hero_text);
        if (heroText != null) {
            String fullText = getString(R.string.selamatDatang_text);
            animateTextPerCharacterLoop(heroText, fullText, 80, 2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop animasi teks supaya tidak berjalan di background
        if (textAnimHandler != null && textAnimRunnable != null) {
            textAnimHandler.removeCallbacks(textAnimRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart animasi teks saat activity resume
        TextView heroText = findViewById(R.id.hero_text);
        if (heroText != null) {
            String fullText = getString(R.string.selamatDatang_text);
            animateTextPerCharacterLoop(heroText, fullText, 80, 2000);
        }
    }

    private void animateTextPerCharacterLoop(TextView textView, String fullText, long delayMillis, long pauseMillis) {
        final Handler handler = new Handler();
        final StringBuilder builder = new StringBuilder();

        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < fullText.length()) {
                    builder.append(fullText.charAt(index));
                    textView.setText(builder.toString());
                    index++;
                    handler.postDelayed(this, delayMillis);
                } else {
                    // Reset untuk loop lagi setelah jeda
                    handler.postDelayed(() -> {
                        builder.setLength(0);
                        index = 0;
                        handler.post(this);
                    }, pauseMillis);
                }
            }
        };

        handler.post(runnable);
    }
}

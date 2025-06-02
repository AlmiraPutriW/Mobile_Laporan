package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnStart, btnBack;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnStart = findViewById(R.id.btnStart);
        btnBack = findViewById(R.id.btnBack);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        btnStart.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password) {
        String url = "https://backend-sipraja.vercel.app/api/v1/user/login";

        progressBar.setVisibility(View.VISIBLE);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        Log.d("LoginActivity", "Response: " + response.toString());

                        String token = null;
                        String userId = null;
                        String name = null; // Tidak ada di response

                        if (response.has("token")) {
                            token = response.getString("token");
                        }

                        if (response.has("userId")) {
                            userId = response.getString("userId");
                        } else if (response.has("data")) {
                            JSONObject data = response.getJSONObject("data");
                            if (data.has("userId")) {
                                userId = data.getString("userId");
                            }
                        }

                        Log.d("LoginActivity", "token=" + token + ", userId=" + userId + ", name=" + name);

                        if (token != null) {
                            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(TOKEN_KEY, token);
                            editor.putString(USER_ID_KEY, userId);
                            editor.putString(USER_NAME_KEY, name); // tetap disimpan meskipun null
                            editor.apply();

                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, BaseActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login gagal: Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal memproses respon", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                    String errorMsg = (statusCode == 400) ? "Email atau password salah" : "Terjadi kesalahan: " + statusCode;
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
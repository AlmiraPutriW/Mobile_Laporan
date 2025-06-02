package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ImageView imageProfile;
    private TextView textHeaderName, textNama, textEmail, textTelepon;
    private LinearLayout ubahProfile;
    private LinearLayout ubahPasswordButton;
    private LinearLayout buttonLogout;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    private String userId;  // simpan userId agar bisa dipakai di tombol ubah password

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi view
        imageProfile = view.findViewById(R.id.imageProfile);
        textHeaderName = view.findViewById(R.id.textHeaderName);
        textNama = view.findViewById(R.id.textNama);
        textEmail = view.findViewById(R.id.textEmail);
        textTelepon = view.findViewById(R.id.textTelepon);
        ubahProfile = view.findViewById(R.id.ubahProfile);
        ubahPasswordButton = view.findViewById(R.id.ubahPasswordButton);
        buttonLogout = view.findViewById(R.id.button_logout); // Inisialisasi tombol logout

        // Ambil token dan decode userId
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        userId = getUserIdFromToken(token);

        Log.d(TAG, "User ID yang didapat dari token: " + userId);

        // Set listener tombol ubah password
        ubahPasswordButton.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(getActivity(), "Gagal mendapatkan ID user. Silakan login ulang.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log userId yang dikirim ke ChangePasswordFragment
            Log.d(TAG, "Mengirim userId ke ChangePasswordFragment: " + userId);

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
            changePasswordFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, changePasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set listener tombol ubah password
        ubahProfile.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(getActivity(), "Gagal mendapatkan ID user. Silakan login ulang.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Log userId yang dikirim ke ChangePasswordFragment
            Log.d(TAG, "Mengirim userId ke ubah profile: " + userId);

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            UbahProfileFragment ubahProfileFragment = new UbahProfileFragment();
            ubahProfileFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ubahProfileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Set listener tombol logout
        buttonLogout.setOnClickListener(v -> {
            if (token == null) {
                Toast.makeText(getActivity(), "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show();
                return;
            }

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://backend-sipraja.vercel.app/api/v1/user/logout")
                    .post(okhttp3.internal.Util.EMPTY_REQUEST) // Kirim POST kosong
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Gagal logout: koneksi gagal", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        // Hapus token
                        prefs.edit().remove(TOKEN_KEY).apply();

                        Toast.makeText(getActivity(), "Berhasil logout", Toast.LENGTH_SHORT).show();

                        // Arahkan ke halaman login jika ada
                        // Contoh jika punya LoginActivity:
                        Intent intent = new Intent(getActivity(), DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        // Atau tutup activity saat ini
                        getActivity().finish();
                    });
                }
            });
        });

        // Ambil data profil dari server
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);

        if (token == null) {
            Toast.makeText(getActivity(), "Token tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://backend-sipraja.vercel.app/api/v1/user/" + userId;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;

                String jsonResponse = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject data = jsonObject.getJSONObject("data");

                        final String name = data.optString("nama", "Tidak ada nama");
                        final String email = data.optString("email", "Tidak ada email");
                        final String phone = data.optString("telp", "Tidak ada nomor telepon");
                        final String imagePath = data.optString("image", "");

                        final String photoUrl = imagePath.startsWith("http")
                                ? imagePath
                                : "https://backend-sipraja.vercel.app" + imagePath;

                        getActivity().runOnUiThread(() -> {
                            textHeaderName.setText(name);
                            textNama.setText(name);
                            textEmail.setText(email);
                            textTelepon.setText(phone);

                            if (!imagePath.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_about)
                                        .into(imageProfile);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Gagal parsing data profil", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Gagal memuat data: " + jsonResponse, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private String getUserIdFromToken(String token) {
        try {
            if (token == null) return null;
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = parts[1];

            // Tambahkan padding jika panjangnya tidak kelipatan 4
            int paddingLength = (4 - (payload.length() % 4)) % 4;
            payload += "=".repeat(paddingLength);

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedPayload = new String(decodedBytes);

            JSONObject jsonObject = new JSONObject(decodedPayload);
            return jsonObject.optString("id", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

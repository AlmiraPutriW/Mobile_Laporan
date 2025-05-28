package com.example.mobilelaporanapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    private ImageView imageProfile;
    private TextView textHeaderName, textNama, textEmail, textTelepon;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.activity_profile, container, false);
        // Catatan: bisa gunakan layout khusus fragment jika ingin pisah,
        // tapi untuk contoh ini tetap memakai layout activity_profile
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageProfile = view.findViewById(R.id.imageProfile);
        textHeaderName = view.findViewById(R.id.textHeaderName);
        textNama = view.findViewById(R.id.textNama);
        textEmail = view.findViewById(R.id.textEmail);
        textTelepon = view.findViewById(R.id.textTelepon);

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

        String userId = getUserIdFromToken(token);
        if (userId == null) {
            Toast.makeText(getActivity(), "Gagal mendapatkan ID dari token.", Toast.LENGTH_SHORT).show();
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
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonResponse = response.body().string();

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

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(() -> {
                            textHeaderName.setText(name);
                            textNama.setText(name);
                            textEmail.setText(email);
                            textTelepon.setText(phone);

                            if (!imagePath.isEmpty()) {
                                Glide.with(requireContext());
                                Glide.with(requireContext())
                                        .load(photoUrl)
                                        .placeholder(R.drawable.ic_about)
                                        .into(imageProfile);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Gagal parsing data profil", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Gagal memuat data: " + jsonResponse, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private String getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = parts[1];

            // Tambahkan padding jika diperlukan
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

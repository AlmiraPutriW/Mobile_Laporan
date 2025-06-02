package com.example.mobilelaporanapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UbahProfileFragment extends Fragment {

    private static final String TAG = "UbahProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText etName, etEmail, etPhone;
    private ImageView ivUploadPhoto;
    private Button btnUpdate, btnCancel;
    private Uri selectedImageUri;
    private String userId;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_update_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        ivUploadPhoto = view.findViewById(R.id.ivUploadPhoto);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnCancel = view.findViewById(R.id.btnCancel);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            Log.d(TAG, "UserId from arguments: " + userId);
        } else {
            Log.e(TAG, "No arguments found");
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Activity.MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return view;
        }
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show();
            return view;
        }

        getUserProfile(userId);

        ivUploadPhoto.setOnClickListener(v -> openImagePicker());
        btnUpdate.setOnClickListener(v -> updateUserProfile());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void getUserProfile(String id) {
        String url = "https://backend-sipraja.vercel.app/api/v1/user/" + id;
        Log.d(TAG, "GET Profile URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject data = response.getJSONObject("data");

                        etName.setText(data.optString("nama", ""));
                        etEmail.setText(data.optString("email", ""));
                        etPhone.setText(String.valueOf(data.optLong("telp")));

                        String imageUrl = data.optString("image", null);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_account)
                                    .into(ivUploadPhoto);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Volley GET error: " + error.toString());
                    Toast.makeText(getContext(), "Gagal mengambil data profil", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivUploadPhoto.setImageURI(selectedImageUri);
        }
    }

    private void updateUserProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gunakan POST dan akali PUT lewat _method
        String url = "https://backend-sipraja.vercel.app/api/v1/user/profile/" + userId;
        Log.d(TAG, "POST (with _method=PUT) Update Profile URL: " + url);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Response: " + new String(response.data));

                    // Pindah ke ProfileFragment
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment()) // sesuaikan dengan ID container kamu
                            .addToBackStack(null)
                            .commit();
                },

                error -> {
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error status code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Error response: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(getContext(), "Gagal update profil", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama", name);   // pastikan sesuai dengan field backend
                params.put("email", email);
                params.put("telp", phone);
                params.put("_method", "PUT"); // akali method override di backend
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (selectedImageUri != null) {
                    try {
                        byte[] imageBytes = getBytesFromUri(selectedImageUri);
                        params.put("image", new DataPart("profile.jpg", imageBytes, "image/jpeg"));
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to get bytes from image URI", e);
                    }
                }
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(multipartRequest);
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream iStream = requireContext().getContentResolver().openInputStream(uri);
        return IOUtils.toByteArray(iStream);
    }
}

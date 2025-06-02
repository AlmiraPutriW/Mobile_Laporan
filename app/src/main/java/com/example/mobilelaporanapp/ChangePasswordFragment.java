package com.example.mobilelaporanapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String USER_ID_KEY = "userId";
    private static final String BASE_URL = "https://backend-sipraja.vercel.app/api/v1/user/password/";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private Button btnSavePassword;

    private String userId = null;
    private String token = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_change_password, container, false);

        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSavePassword = view.findViewById(R.id.btnSavePassword);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, null);

        if (getArguments() != null && getArguments().containsKey(USER_ID_KEY)) {
            userId = getArguments().getString(USER_ID_KEY);
            Log.d(TAG, "UserId didapat dari arguments: " + userId);
        }

        if (userId == null) {
            userId = prefs.getString(USER_ID_KEY, null);
            Log.d(TAG, "UserId fallback dari SharedPreferences: " + userId);
        }

        btnSavePassword.setOnClickListener(v -> {
            String oldPass = etOldPassword.getText() != null ? etOldPassword.getText().toString().trim() : "";
            String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
            String confirmPass = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (validateInputs(oldPass, newPass, confirmPass)) {
                changePassword(oldPass, newPass, confirmPass);
            }
        });

        return view;
    }

    private boolean validateInputs(String oldPass, String newPass, String confirmPass) {
        if (TextUtils.isEmpty(oldPass)) {
            etOldPassword.setError("Password lama wajib diisi");
            etOldPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(newPass)) {
            etNewPassword.setError("Password baru wajib diisi");
            etNewPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmPass)) {
            etConfirmPassword.setError("Konfirmasi password wajib diisi");
            etConfirmPassword.requestFocus();
            return false;
        }
        if (!newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Password konfirmasi tidak sama");
            etConfirmPassword.requestFocus();
            return false;
        }
        if (newPass.length() < 6) {
            etNewPassword.setError("Password harus minimal 6 karakter");
            etNewPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void changePassword(String oldPass, String newPass, String confirmPass) {
        if (token == null || userId == null) {
            Toast.makeText(requireContext(), "Token atau ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Token atau userId null");
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("oldPassword", oldPass);
            json.put("newPassword", newPass);
            json.put("confirmNewPassword", confirmPass);

            String jsonString = json.toString();
            Log.d(TAG, "Payload JSON: " + jsonString);

            RequestBody body = RequestBody.create(jsonString, JSON);
            String url = BASE_URL + userId;

            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

            Log.d(TAG, "Requesting password change to: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage(), e);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resStr = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response Body: " + resStr);

                    if (response.isSuccessful()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                            etOldPassword.setText("");
                            etNewPassword.setText("");
                            etConfirmPassword.setText("");

                            // Navigasi kembali ke ProfileFragment secara manual
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new ProfileFragment())
                                    .addToBackStack(null)
                                    .commit();
                        });
                    } else {
                        String message = "Gagal mengubah password";
                        try {
                            JSONObject errJson = new JSONObject(resStr);
                            if (errJson.has("message")) {
                                message = errJson.getString("message");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing error message", e);
                        }

                        final String finalMessage = message;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), finalMessage, Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON Error", e);
            Toast.makeText(requireContext(), "Terjadi kesalahan internal", Toast.LENGTH_SHORT).show();
        }
    }
}

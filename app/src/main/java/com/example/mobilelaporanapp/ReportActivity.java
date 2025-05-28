package com.example.mobilelaporanapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.Volley;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class ReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText etReporterName, etReportDate, etReportTitle, etLocation, etProblemDescription;
    Spinner spinnerCategory;
    TextView tvUploadPhoto;
    Button btnSubmitReport, btnCancel;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private final String API_URL = "https://backend-sipraja.vercel.app/api/v1/laporan/create";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Inisialisasi view
        etReporterName = findViewById(R.id.etReporterName);
        etReportDate = findViewById(R.id.etReportDate);
        etReportTitle = findViewById(R.id.etReportTitle);
        etLocation = findViewById(R.id.etLocation);
        etProblemDescription = findViewById(R.id.etProblemDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvUploadPhoto = findViewById(R.id.tvUploadPhoto);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnCancel = findViewById(R.id.btnCancel);

        // Request permission jika SDK >= Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }

        // Setup spinner kategori
        List<String> categories = Arrays.asList("Pilih Kategori", "Jembatan", "Jalan", "Lalu Lintas", "Lainnya");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Listener tombol dan textview
        tvUploadPhoto.setOnClickListener(v -> openGallery());
        btnSubmitReport.setOnClickListener(v -> submitReport());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedImageUris.clear();

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            tvUploadPhoto.setText("Total foto terpilih: " + selectedImageUris.size());
        }
    }

    private void submitReport() {
        String nama = etReporterName.getText().toString().trim();
        String tanggal = etReportDate.getText().toString().trim();
        String judul = etReportTitle.getText().toString().trim();
        String kategori = spinnerCategory.getSelectedItem().toString();
        String lokasi = etLocation.getText().toString().trim();
        String deskripsi = etProblemDescription.getText().toString().trim();

        if (nama.isEmpty() || tanggal.isEmpty() || judul.isEmpty() || kategori.equals("Pilih Kategori")
                || lokasi.isEmpty() || deskripsi.isEmpty() || selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Semua field dan minimal satu foto wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengirim laporan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Ambil token dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                VolleyMultipartRequest.Method.POST,
                API_URL,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        String errorString = new String(networkResponse.data);
                        Log.e("UploadError", "Status code: " + networkResponse.statusCode + ", Response: " + errorString);
                        Toast.makeText(this, "Gagal mengirim laporan: " + errorString, Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("UploadError", error.toString());
                        Toast.makeText(this, "Gagal mengirim laporan", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama", nama);
                params.put("tanggal", tanggal);
                params.put("judul", judul);
                params.put("kategori", kategori);
                params.put("lokasi", lokasi);
                params.put("description", deskripsi);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> fileData = new HashMap<>();
                int index = 0;
                for (Uri uri : selectedImageUris) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[16384];
                        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        byte[] imageData = buffer.toByteArray();
                        inputStream.close();

                        // Semua gambar dikirim dengan key "gambar_pendukung"
                        fileData.put("gambar_pendukung", new DataPart("image" + index + ".jpg", imageData, "image/jpeg"));
                        index++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return fileData;
            }
        };

        Volley.newRequestQueue(this).add(multipartRequest);
    }
}


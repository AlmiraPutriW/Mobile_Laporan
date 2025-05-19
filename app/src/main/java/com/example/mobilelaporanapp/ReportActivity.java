package com.example.mobilelaporanapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    EditText etReporterName, etReportDate, etReportTitle, etReportCategory, etLocation, etProblemDescription;
    Button btnSubmitReport, btnCancel;

    private final String API_URL = "https://backend-sipraja.vercel.app/api/v1/laporan/create";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report); // Sesuaikan nama layout-mu

        etReporterName = findViewById(R.id.etReporterName);
        etReportDate = findViewById(R.id.etReportDate);
        etReportTitle = findViewById(R.id.etReportTitle);
        etReportCategory = findViewById(R.id.etReportCategory);
        etLocation = findViewById(R.id.etLocation);
        etProblemDescription = findViewById(R.id.etProblemDescription);

        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnCancel = findViewById(R.id.btnCancel);

        btnSubmitReport.setOnClickListener(v -> submitReport());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitReport() {
        String reporterName = etReporterName.getText().toString().trim();
        String reportDate = etReportDate.getText().toString().trim();
        String reportTitle = etReportTitle.getText().toString().trim();
        String reportCategory = etReportCategory.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etProblemDescription.getText().toString().trim();

        if (reporterName.isEmpty() || reportDate.isEmpty() || reportTitle.isEmpty() ||
                reportCategory.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengirim laporan...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_URL,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.optBoolean("success", false);
                        if (success) {
                            Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ReportActivity.this, MenuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Gagal mengirim laporan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama_pelapor", reporterName);
                params.put("tanggal", reportDate);
                params.put("judul_laporan", reportTitle);
                params.put("kategori", reportCategory);
                params.put("lokasi", location);
                params.put("deskripsi", description);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}

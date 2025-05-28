package com.example.mobilelaporanapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportResponse;
import com.example.mobilelaporanapp.network.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;  // retrofit2.Response untuk callback Retrofit
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportListActivity extends AppCompatActivity {

    private static final String TAG = "ReportListActivity";
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    private EditText etSearchReport;
    private RadioGroup rgStatus, rgKategori;
    private RecyclerView rvReports;
    private Button btnCreateReport;

    private ReportAdapter reportAdapter;
    private final List<Report> fullReportList = new ArrayList<>();

    private String currentStatusFilter = "semua";
    private String currentKategoriFilter = "semua";

    private ApiService apiService;
    private String token; // token dari SharedPreferences

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listlaporan);

        initViews();
        loadToken();
        setupRetrofit();
        setupRecyclerView();
        loadReportsFromApi();
        setupFilters();
        setupSearch();
        setupCreateButton();
    }

    private void initViews() {
        etSearchReport = findViewById(R.id.etSearchReport);
        rgStatus = findViewById(R.id.rgStatus);
        rgKategori = findViewById(R.id.rgKategori);
        rvReports = findViewById(R.id.rvReports);
        btnCreateReport = findViewById(R.id.btnCreateReport);
    }

    private void loadToken() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "Token di-load: " + (token != null ? "[TOKEN ADA]" : "null"));
    }

    private void setupRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        if (token == null || token.isEmpty()) {
                            // Jika token tidak ada, lanjut request tanpa header Authorization
                            return chain.proceed(originalRequest);
                        }

                        // Tambahkan header Authorization dengan Bearer token
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-sipraja.vercel.app/")
                .client(client)  // Pasang OkHttpClient dengan interceptor di sini
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportAdapter(new ArrayList<>());
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportAdapter);
    }

    private void loadReportsFromApi() {
        apiService.getReports().enqueue(new Callback<ReportResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportResponse> call, @NonNull Response<ReportResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullReportList.clear();
                    fullReportList.addAll(response.body().getMessage());
                    Collections.sort(fullReportList, (r1, r2) -> r2.getTanggal().compareTo(r1.getTanggal()));
                    filterReports();
                    Log.d(TAG, "Data laporan berhasil dimuat, jumlah: " + fullReportList.size());
                } else {
                    String errorBody = "null";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "Gagal memuat data laporan. Response code: " + response.code() + ", errorBody: " + errorBody);
                    Toast.makeText(ReportListActivity.this, "Gagal memuat data laporan", Toast.LENGTH_SHORT).show();

                    // Jika 401 Unauthorized, bisa handle logout / minta login ulang
                    if (response.code() == 401) {
                        Toast.makeText(ReportListActivity.this, "Token tidak valid atau habis, silakan login ulang", Toast.LENGTH_LONG).show();
                        // Contoh bisa redirect ke LoginActivity jika perlu
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loadReportsFromApi: " + t.getMessage(), t);
                Toast.makeText(ReportListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStatusSelesai) {
                currentStatusFilter = "selesai";
            } else if (checkedId == R.id.rbStatusDiproses) {
                currentStatusFilter = "di proses";
            } else if (checkedId == R.id.rbStatusBelumDiproses) {
                currentStatusFilter = "belum di proses";
            } else {
                currentStatusFilter = "semua";
            }
            filterReports();
        });

        rgKategori.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbKategoriJalan) {
                currentKategoriFilter = "jalan";
            } else if (checkedId == R.id.rbKategoriJembatan) {
                currentKategoriFilter = "jembatan";
            } else if (checkedId == R.id.rbKategoriLaluLintas) {
                currentKategoriFilter = "lalu lintas";
            } else {
                currentKategoriFilter = "semua";
            }
            filterReports();
        });
    }

    private void setupSearch() {
        etSearchReport.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports();
            }
        });
    }


    private void setupCreateButton() {
        btnCreateReport.setOnClickListener(v -> {
            Intent intent = new Intent(ReportListActivity.this, ReportFragment.class);
            startActivity(intent);
        });
    }

    private void filterReports() {
        String query = etSearchReport.getText().toString().toLowerCase().trim();
        List<Report> filteredList = new ArrayList<>();

        for (Report report : fullReportList) {
            boolean statusMatches = currentStatusFilter.equals("semua") ||
                    report.getStatus().equalsIgnoreCase(currentStatusFilter);
            boolean kategoriMatches = currentKategoriFilter.equals("semua") ||
                    report.getKategori().equalsIgnoreCase(currentKategoriFilter);
            boolean queryMatches = query.isEmpty() ||
                    report.getJudul().toLowerCase().contains(query) ||
                    report.getDeskripsi().toLowerCase().contains(query) ||
                    report.getLokasi().toLowerCase().contains(query);

            if (statusMatches && kategoriMatches && queryMatches) {
                filteredList.add(report);
            }
        }

        reportAdapter.updateList(filteredList);
    }
}

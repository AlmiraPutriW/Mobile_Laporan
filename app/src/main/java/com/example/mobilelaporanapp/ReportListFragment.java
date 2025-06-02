package com.example.mobilelaporanapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportResponse;
import com.example.mobilelaporanapp.network.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportListFragment extends Fragment {

    private static final String TAG = "ReportListFragment";
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
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_listlaporan, container, false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadToken();
        setupRetrofit();
        setupRecyclerView();
        loadReportsFromApi();
        setupFilters();
        setupSearch();
        setupCreateButton();
    }

    private void initViews(View view) {
        etSearchReport = view.findViewById(R.id.etSearchReport);
        rgStatus = view.findViewById(R.id.rgStatus);
        rgKategori = view.findViewById(R.id.rgKategori);
        rvReports = view.findViewById(R.id.rvReports);
        btnCreateReport = view.findViewById(R.id.btnCreateReport);
    }

    private void loadToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "Token di-load: " + (token != null ? "[TOKEN ADA]" : "null"));
    }

    private void setupRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();

                    if (token == null || token.isEmpty()) {
                        return chain.proceed(originalRequest);
                    }

                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();

                    return chain.proceed(newRequest);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-sipraja.vercel.app/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportAdapter(new ArrayList<>());
        rvReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReports.setAdapter(reportAdapter);

        // Pasang listener klik detail
        reportAdapter.setOnDetailClickListener(report -> {
            Fragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("laporan_id", report.getId());
            detailFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();

        });
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
                    Log.e(TAG, "Gagal memuat data laporan. Code: " + response.code() + ", errorBody: " + errorBody);
                    Toast.makeText(requireContext(), "Gagal memuat data laporan", Toast.LENGTH_SHORT).show();

                    if (response.code() == 401) {
                        Toast.makeText(requireContext(), "Token tidak valid, silakan login ulang", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loadReportsFromApi: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, new ReportFragment()); // ganti R.id.fragment_container dengan ID dari FragmentContainerView kamu
            transaction.addToBackStack(null);
            transaction.commit();
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

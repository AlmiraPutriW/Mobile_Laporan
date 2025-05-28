package com.example.mobilelaporanapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportResponse;
import com.example.mobilelaporanapp.network.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReportUserFragment extends Fragment {

    private static final String TAG = "ReportUserFragment";
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    private EditText etSearchReport;
    private RadioGroup rgStatus, rgKategori;
    private RecyclerView rvReports;
    private Button btnCreateReport;

    private ReportAdapter reportAdapter;
    private final List<Report> fullReportList = new ArrayList<>();
    private final List<Report> filteredReportList = new ArrayList<>();

    private String currentStatusFilter = "semua";
    private String currentKategoriFilter = "semua";

    private ApiService apiService;
    private String token;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_listlaporan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadUserSession();

        if (token == null || userId == null) {
            Toast.makeText(requireContext(), "Token atau User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        setupRetrofit();
        setupRecyclerView();
        setupFilters();
        setupSearchFunctionality();
        setupCreateReportButton();
        loadReportsFromApi();
    }

    private void initViews(View view) {
        etSearchReport = view.findViewById(R.id.etSearchReport);
        rgStatus = view.findViewById(R.id.rgStatus);
        rgKategori = view.findViewById(R.id.rgKategori);
        rvReports = view.findViewById(R.id.rvReports);
        btnCreateReport = view.findViewById(R.id.btnCreateReport);
    }

    private void loadUserSession() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, null);
        userId = token != null ? getUserIdFromToken(token) : null;

        Log.d(TAG, "Loaded token: " + token + ", userId: " + userId);
    }

    private String getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = parts[1];
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);

            JSONObject jsonObject = new JSONObject(decodedPayload);
            return jsonObject.optString("id", null);
        } catch (JSONException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    if (token != null && !token.isEmpty()) {
                        Request requestWithToken = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(requestWithToken);
                    }
                    return chain.proceed(original);
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

        reportAdapter.setOnDetailClickListener(report -> {
            DetailUserFragment detailuserFragment = new DetailUserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("laporan_id", report.getId());
            detailuserFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailuserFragment)
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

                    for (Report report : response.body().getMessage()) {
                        if (userId != null && userId.equals(report.getUserId())) {
                            fullReportList.add(report);
                        }
                    }

                    fullReportList.sort((r1, r2) -> r2.getTanggal().compareTo(r1.getTanggal()));
                    filterReports();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading reports: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Gagal memuat laporan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        String errorBody = "null";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "Failed to load reports. Code: " + response.code() + ", errorBody: " + errorBody);
        Toast.makeText(requireContext(), "Gagal memuat data laporan", Toast.LENGTH_SHORT).show();

        if (response.code() == 401) {
            Toast.makeText(requireContext(), "Token tidak valid. Silakan login ulang.", Toast.LENGTH_LONG).show();
        }
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

    private void setupSearchFunctionality() {
        etSearchReport.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports();
            }
        });
    }

    private void filterReports() {
        String searchQuery = etSearchReport.getText().toString().toLowerCase().trim();
        filteredReportList.clear();

        for (Report report : fullReportList) {
            boolean matchesStatus = currentStatusFilter.equals("semua") ||
                    report.getStatus().equalsIgnoreCase(currentStatusFilter);

            boolean matchesKategori = currentKategoriFilter.equals("semua") ||
                    report.getKategori().equalsIgnoreCase(currentKategoriFilter);

            boolean matchesSearch = searchQuery.isEmpty() ||
                    report.getJudul().toLowerCase().contains(searchQuery);

            if (matchesStatus && matchesKategori && matchesSearch) {
                filteredReportList.add(report);
            }
        }

        reportAdapter.setReportList(filteredReportList);
        reportAdapter.notifyDataSetChanged();
    }

    private void setupCreateReportButton() {
        btnCreateReport.setOnClickListener(v -> {
            ReportFragment createReportFragment = new ReportFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createReportFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}

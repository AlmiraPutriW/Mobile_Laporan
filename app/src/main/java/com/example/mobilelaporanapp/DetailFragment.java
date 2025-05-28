package com.example.mobilelaporanapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportDetailResponse;
import com.example.mobilelaporanapp.network.ApiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "token";

    private ImageView ivGambar;
    private TextView tvJudul, tvTanggal, tvLokasi, tvKategori, tvStatus, tvDeskripsi;

    private ApiService apiService;
    private String token;
    private String laporanId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detail_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        getLaporanIdFromArguments();

        if (laporanId == null) {
            Toast.makeText(requireContext(), "ID laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        loadToken();
        setupRetrofit();
        fetchDetailLaporan();
    }

    private void initViews(View view) {
        ivGambar = view.findViewById(R.id.ivGambar);
        tvJudul = view.findViewById(R.id.tvJudul);
        tvTanggal = view.findViewById(R.id.tvTanggal);
        tvLokasi = view.findViewById(R.id.tvLokasi);
        tvKategori = view.findViewById(R.id.tvKategori);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvDeskripsi = view.findViewById(R.id.tvDeskripsi);
    }

    private void getLaporanIdFromArguments() {
        if (getArguments() != null) {
            laporanId = getArguments().getString("laporan_id");
            Log.d(TAG, "Memuat detail untuk laporan ID: " + laporanId);
        }
    }

    private void loadToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "Token di-load: " + (token != null ? "[TOKEN ADA]" : "null"));
    }

    private void setupRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, "HTTP: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

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
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://backend-sipraja.vercel.app/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void fetchDetailLaporan() {
        apiService.getReportById(laporanId).enqueue(new Callback<ReportDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportDetailResponse> call,
                                   @NonNull Response<ReportDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReportDetailResponse responseBody = response.body();

                    Log.d(TAG, "Message dari API: " + responseBody.getMessage());

                    if (responseBody.getLaporan() != null) {
                        tampilkanDetail(responseBody.getLaporan());
                        Log.d(TAG, "Detail laporan berhasil dimuat");
                    } else {
                        Toast.makeText(requireContext(), "Data laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "laporan null");
                    }
                } else {
                    Log.e(TAG, "Response gagal: " + response.message());
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportDetailResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Terjadi kesalahan saat memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTanggal(String tanggal) {
        if (tanggal == null || tanggal.isEmpty()) return "-";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = inputFormat.parse(tanggal);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return date != null ? outputFormat.format(date) : tanggal;
        } catch (ParseException e) {
            Log.e(TAG, "Format tanggal error", e);
            return tanggal;
        }
    }

    private void tampilkanDetail(Report report) {
        tvJudul.setText(nonNullOrDash(report.getJudul()));
        tvTanggal.setText(nonNullOrDash(report.getTanggal())); // atau formatTanggal(report.getCreatedAt())
        tvLokasi.setText(nonNullOrDash(report.getLokasi()));
        tvKategori.setText(nonNullOrDash(report.getKategori()));
        tvStatus.setText(nonNullOrDash(report.getStatus()));
        tvDeskripsi.setText(nonNullOrDash(report.getDeskripsi()));

        List<String> gambarList = report.getGambarPendukung();
        if (gambarList != null && !gambarList.isEmpty()) {
            Glide.with(requireContext())
                    .load(gambarList.get(0))
                    .placeholder(R.drawable.ic_info)
                    .error(R.drawable.ic_loc)
                    .into(ivGambar);
        } else {
            ivGambar.setImageResource(R.drawable.ic_info);
        }
    }


    private String nonNullOrDash(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "-";
    }
}

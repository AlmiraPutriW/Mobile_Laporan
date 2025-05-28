package com.example.mobilelaporanapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportDetailResponse;
import com.example.mobilelaporanapp.network.ApiClient;
import com.example.mobilelaporanapp.network.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditReportFragment extends Fragment {

    private static final String ARG_REPORT_ID = "report_id";
    private static final String TAG = "EditReportFragment";
    private static final int PICK_IMAGE_REQUEST = 100;

    private String reportId;
    private EditText etReporterName, etReportDate, etReportTitle, etLocation, etProblemDescription;
    private Spinner spinnerCategory;
    private TextView tvUploadPhoto;
    private ImageView ivPhotoPreview;

    private Uri selectedImageUri = null;
    private File selectedImageFile = null;

    public EditReportFragment() {}

    public static EditReportFragment newInstance(String reportId) {
        EditReportFragment fragment = new EditReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT_ID, reportId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reportId = getArguments().getString(ARG_REPORT_ID);
            Log.d(TAG, "onCreate - reportId: " + reportId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit, container, false);

        etReporterName = view.findViewById(R.id.etReporterName);
        etReportDate = view.findViewById(R.id.etReportDate);
        etReportTitle = view.findViewById(R.id.etReportTitle);
        etLocation = view.findViewById(R.id.etLocation);
        etProblemDescription = view.findViewById(R.id.etProblemDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        tvUploadPhoto = view.findViewById(R.id.tvUploadPhotoHint);
        ivPhotoPreview = view.findViewById(R.id.ivUploadPhoto);

        setupCategorySpinner();

        tvUploadPhoto.setOnClickListener(v -> openImagePicker());

        if (reportId != null && !reportId.isEmpty()) {
            loadReportData(reportId);
        } else {
            Toast.makeText(requireContext(), "ID laporan tidak ditemukan.", Toast.LENGTH_SHORT).show();
        }

        view.findViewById(R.id.btnSubmitReport).setOnClickListener(v -> updateReport());

        return view;
    }

    private void setupCategorySpinner() {
        String[] kategoriList = {"Pilih Kategori", "Jembatan", "Jalan", "Lalu Lintas", "Lainnya"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, kategoriList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .into(ivPhotoPreview);

            selectedImageFile = createFileFromUri(selectedImageUri);
            tvUploadPhoto.setText(getFileName(selectedImageUri));
        }
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            String fileName = getFileName(uri);
            if (fileName == null) fileName = "temp_image";

            File tempFile = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void loadReportData(String id) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Token tidak tersedia. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<ReportDetailResponse> call = apiService.getReportById(id);

        call.enqueue(new Callback<ReportDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportDetailResponse> call, @NonNull Response<ReportDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Report report = response.body().getLaporan();
                    if (report != null) {
                        populateFields(report);
                    } else {
                        Toast.makeText(getContext(), "Data laporan kosong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data laporan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(Report report) {
        etReporterName.setText(report.getNama());
        etReportDate.setText(report.getTanggal());
        etReportTitle.setText(report.getJudul());
        etLocation.setText(report.getLokasi());
        etProblemDescription.setText(report.getDeskripsi());

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
        int position = adapter.getPosition(report.getKategori());
        if (position >= 0) {
            spinnerCategory.setSelection(position);
        }

        if (report.getGambarPendukung() != null && !report.getGambarPendukung().isEmpty()) {
            String imageUrl = report.getGambarPendukung().get(0);
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_info)
                    .into(ivPhotoPreview);
            tvUploadPhoto.setText("Klik di sini untuk mengganti foto");
        }
    }

    private void updateReport() {
        String nama = etReporterName.getText().toString().trim();
        String tanggal = etReportDate.getText().toString().trim();
        String judul = etReportTitle.getText().toString().trim();
        String lokasi = etLocation.getText().toString().trim();
        String kategori = spinnerCategory.getSelectedItem().toString();
        String description = etProblemDescription.getText().toString().trim();

        if (nama.isEmpty() || tanggal.isEmpty() || judul.isEmpty() || lokasi.isEmpty()
                || description.isEmpty() || kategori.equals("Pilih Kategori")) {
            Toast.makeText(getContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Token tidak tersedia. Silakan login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        MultipartBody.Part imagePart = null;
        if (selectedImageFile != null && selectedImageFile.exists()) {
            RequestBody requestFile = RequestBody.create(selectedImageFile, MediaType.parse("image/*"));
            imagePart = MultipartBody.Part.createFormData("gambar_pendukung", selectedImageFile.getName(), requestFile);
        }

        RequestBody namaBody = RequestBody.create(nama, MediaType.parse("text/plain"));
        RequestBody tanggalBody = RequestBody.create(tanggal, MediaType.parse("text/plain"));
        RequestBody judulBody = RequestBody.create(judul, MediaType.parse("text/plain"));
        RequestBody lokasiBody = RequestBody.create(lokasi, MediaType.parse("text/plain"));
        RequestBody kategoriBody = RequestBody.create(kategori, MediaType.parse("text/plain"));
        RequestBody descriptionBody = RequestBody.create(description, MediaType.parse("text/plain"));

        Call<ReportDetailResponse> call = apiService.updateReport(
                reportId, namaBody, tanggalBody, judulBody, lokasiBody,
                kategoriBody, descriptionBody, imagePart);

        call.enqueue(new Callback<ReportDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReportDetailResponse> call, @NonNull Response<ReportDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Laporan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Update berhasil: " + response.body());
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new ReportUserFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal memperbarui laporan", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update gagal: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Update error", t);
            }
        });
    }
}
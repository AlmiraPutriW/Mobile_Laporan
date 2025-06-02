package com.example.mobilelaporanapp.network;

import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.UserResponse;
import com.example.mobilelaporanapp.model.ReportResponse;
import com.example.mobilelaporanapp.model.ReportDetailResponse;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("api/v1/user/{id}")
    Call<UserResponse> getUser(@Path("id") String id);

    @GET("api/v1/laporan")
    Call<ReportResponse> getReports();

    @GET("api/v1/laporan/{id}")
    Call<ReportDetailResponse> getReportById(@Path("id") String id);

    @GET("api/v1/laporan")
    Call<ReportResponse> getReportsByUserId(@Query("userId") String userId);

    @Multipart
    @PUT("api/v1/laporan/{id}")
    Call<ReportDetailResponse> updateReport(
            @Path("id") String reportId,
            @Part("nama") RequestBody nama,
            @Part("tanggal") RequestBody tanggal,
            @Part("judul") RequestBody judul,
            @Part("lokasi") RequestBody lokasi,
            @Part("kategori") RequestBody kategori,
            @Part("description") RequestBody description,         // Perhatikan 'description'
            @Part MultipartBody.Part gambar_pendukung             // Perhatikan 'gambar_pendukung'
    );

    @Multipart
    @PUT("api/v1/laporan/{id}")
    Call<ReportDetailResponse> updateReportWithImage(
            @Path("id") String id,
            @Part("nama") RequestBody nama,
            @Part("tanggal") RequestBody tanggal,
            @Part("judul") RequestBody judul,
            @Part("lokasi") RequestBody lokasi,
            @Part("kategori") RequestBody kategori,
            @Part("deskripsi") RequestBody description,
            @Part MultipartBody.Part gambar_pendukung
    );

    @Multipart
    @PUT("api/v1/laporan/{id}")
    Call<ReportDetailResponse> updateReportWithoutImage(
            @Path("id") String id,
            @Part("nama") RequestBody nama,
            @Part("tanggal") RequestBody tanggal,
            @Part("judul") RequestBody judul,
            @Part("lokasi") RequestBody lokasi,
            @Part("kategori") RequestBody kategori,
            @Part("deskripsi") RequestBody deskripsi
    );

    @DELETE("api/v1/laporan/delete/{id}")
    Call<Void> deleteReport(@Path("id") String id);

}

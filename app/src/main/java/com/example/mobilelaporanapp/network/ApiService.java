package com.example.mobilelaporanapp.network;

import com.example.mobilelaporanapp.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/v1/user/{id}")
    Call<UserResponse> getUser(@Path("id") String id);
}

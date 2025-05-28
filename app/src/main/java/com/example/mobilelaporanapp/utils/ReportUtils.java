package com.example.mobilelaporanapp.utils;

import com.example.mobilelaporanapp.model.Report;
import com.example.mobilelaporanapp.model.ReportResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class ReportUtils {

    public static void printAllIdsFromJson(String json) {
        Gson gson = new GsonBuilder().create();

        // Parse JSON ke objek ReportResponse
        ReportResponse response = gson.fromJson(json, ReportResponse.class);

        // Ambil list laporan
        List<Report> reports = response.getMessage();

        if (reports != null) {
            System.out.println("Daftar _id laporan:");
            for (Report r : reports) {
                System.out.println(r.getId());
            }
        } else {
            System.out.println("Tidak ada data laporan.");
        }
    }
}

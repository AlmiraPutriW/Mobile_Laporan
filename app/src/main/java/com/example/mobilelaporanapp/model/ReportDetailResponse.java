package com.example.mobilelaporanapp.model;

import com.google.gson.annotations.SerializedName;

public class ReportDetailResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("laporan")
    private Report laporan;

    // Getter dan Setter message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter dan Setter laporan
    public Report getLaporan() {
        return laporan;
    }

    public void setLaporan(Report laporan) {
        this.laporan = laporan;
    }
}

package com.example.mobilelaporanapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReportResponse {

    @SerializedName("message")
    private List<Report> message;

    public List<Report> getMessage() {
        return message;
    }

    public void setMessage(List<Report> message) {
        this.message = message;
    }

}

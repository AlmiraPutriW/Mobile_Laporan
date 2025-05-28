package com.example.mobilelaporanapp.model;

import com.example.mobilelaporanapp.R;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Report {

    @SerializedName("_id")
    private String id;

    @SerializedName("nama")
    private String nama;

    @SerializedName("tanggal")
    private String tanggal;

    @SerializedName("judul")
    private String judul;

    @SerializedName("lokasi")
    private String lokasi;

    @SerializedName("kategori")
    private String kategori;

    @SerializedName("status")
    private String status;

    @SerializedName("description")
    private String deskripsi;

    @SerializedName("gambar_pendukung")
    private List<String> gambarPendukung;

    @SerializedName("userId")
    private String userId;

    // ✅ Constructor kosong
    public Report() {}

    // Constructor lengkap
    public Report(String id, String nama, String tanggal, String judul, String lokasi,
                  String kategori, String status, String deskripsi, List<String> gambarPendukung, String userId) {
        this.id = id;
        this.nama = nama;
        this.tanggal = tanggal;
        this.judul = judul;
        this.lokasi = lokasi;
        this.kategori = kategori;
        this.status = status;
        this.deskripsi = deskripsi;
        this.gambarPendukung = gambarPendukung;
        this.userId = userId;
    }

    // ... (getter & setter tetap sama)


    // Getter untuk userId
    public String getUserId() {
        return userId;
    }

    // Setter untuk userId (opsional)
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getTanggal() { return tanggal; }
    public String getJudul() { return judul; }
    public String getLokasi() { return lokasi; }
    public String getKategori() { return kategori; }
    public String getStatus() { return status; }
    public String getDeskripsi() { return deskripsi; }
    public List<String> getGambarPendukung() { return gambarPendukung; }


    // Setter
    public void setId(String id) { this.id = id; }
    public void setNama(String nama) { this.nama = nama; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public void setJudul(String judul) { this.judul = judul; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setStatus(String status) { this.status = status; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setGambarPendukung(List<String> gambarPendukung) { this.gambarPendukung = gambarPendukung; }



    /**
     * Mengembalikan resource icon berdasarkan kategori laporan.
     * Default icon adalah ic_info jika kategori tidak dikenali atau null.
     */
    public int getIconResId() {
        if (kategori == null) return R.drawable.ic_info;

        switch (kategori.toLowerCase()) {
            case "jalan":
                return R.drawable.ic_info;      // Ganti dengan ikon jalan jika ada
            case "jembatan":
                return R.drawable.ic_info;      // Ganti dengan ikon jembatan jika ada
            case "lalu lintas":
                return R.drawable.ic_info;      // Ganti dengan ikon lalu lintas jika ada
            default:
                return R.drawable.ic_info;
        }
    }
}

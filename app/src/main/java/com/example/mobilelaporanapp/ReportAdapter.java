package com.example.mobilelaporanapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilelaporanapp.model.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private OnDetailClickListener detailClickListener;
    public void setReportList(List<Report> filteredReportList) {
        if (filteredReportList != null) {
            this.reportList.clear();
            this.reportList.addAll(filteredReportList);
            notifyDataSetChanged();
        }
    }


    // Interface untuk callback klik detail
    public interface OnDetailClickListener {
        void onDetailClick(Report report);
    }

    // Setter listener dari luar adapter
    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.detailClickListener = listener;
    }

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        // Set data ke views
        holder.tvJudul.setText(report.getJudul());
        holder.tvKategori.setText(report.getKategori());
        holder.tvTanggal.setText(report.getTanggal());
        holder.tvLokasi.setText(report.getLokasi());
        holder.tvStatus.setText(report.getStatus());
        String fullDeskripsi = report.getDeskripsi();
        String shortDeskripsi = fullDeskripsi.length() > 100 ? fullDeskripsi.substring(0, 100) + "..." : fullDeskripsi;
        holder.tvDeskripsi.setText(shortDeskripsi);

        if (report.getGambarPendukung() != null && !report.getGambarPendukung().isEmpty()) {
            String imageUrl = report.getGambarPendukung().get(0); // Ambil gambar pertama
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_info)  // opsional
                    .error(R.drawable.ic_about)       // opsional
                    .into(holder.ivGambarPendukung);
        } else {
            holder.ivGambarPendukung.setImageResource(R.drawable.ic_info);
        }

        // Set click listener untuk tombol detail
        holder.btnLihatDetail.setOnClickListener(v -> {
            if (detailClickListener != null) {
                detailClickListener.onDetailClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    // Method untuk update data adapter
    public void updateList(List<Report> newList) {
        reportList.clear();
        reportList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder inner class
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGambarPendukung;
        TextView tvJudul, tvKategori, tvTanggal, tvLokasi, tvStatus, tvDeskripsi;
        ImageButton btnLihatDetail;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGambarPendukung = itemView.findViewById(R.id.ivGambarPendukung);
            tvJudul = itemView.findViewById(R.id.tvJudul);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            btnLihatDetail = itemView.findViewById(R.id.btnLihatDetail); // ImageButton di layout item_report.xml
        }
    }
}

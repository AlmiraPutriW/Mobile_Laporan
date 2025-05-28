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

public class EditAdapter extends RecyclerView.Adapter<EditAdapter.ReportViewHolder> {

    private List<Report> reportList;
    private OnDetailClickListener detailClickListener;
    private OnEditClickListener editClickListener;

    public EditAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

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

    public interface OnEditClickListener {
        void onEditClick(Report report);
    }

    // Setter listener dari luar adapter
    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.detailClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit, parent, false);
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
                    .placeholder(R.drawable.ic_info)
                    .error(R.drawable.ic_about)
                    .into(holder.ivGambarPendukung);
        } else {
            holder.ivGambarPendukung.setImageResource(R.drawable.ic_info);
        }

        // Set click listener untuk tombol lihat detail
        holder.btnLihatDetail.setOnClickListener(v -> {
            if (detailClickListener != null) {
                detailClickListener.onDetailClick(report);
            }
        });

        // Set click listener untuk tombol edit
        holder.btnEditLaporan.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void updateList(List<Report> newList) {
        reportList.clear();
        reportList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnEditLaporan;
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
            btnLihatDetail = itemView.findViewById(R.id.btnLihatDetail);
            btnEditLaporan = itemView.findViewById(R.id.btnEditLaporan);
        }
    }
}

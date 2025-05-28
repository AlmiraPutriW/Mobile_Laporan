package com.example.mobilelaporanapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class DashboardFragment extends Fragment {

    private MaterialButton reportButton;

    public DashboardFragment() {
        // Konstruktor kosong
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        // Inisialisasi tombol
        reportButton = view.findViewById(R.id.report_button);

        reportButton.setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences("MY_APP", requireActivity().MODE_PRIVATE);
            prefs.edit().remove("TOKEN").apply();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}

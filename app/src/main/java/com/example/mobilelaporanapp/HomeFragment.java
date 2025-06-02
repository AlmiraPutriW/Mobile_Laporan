package com.example.mobilelaporanapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class HomeFragment extends Fragment {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map;

    private Handler textAnimHandler = new Handler();
    private Runnable textAnimRunnable;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set user agent untuk osmdroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Inflate layout
        View view = inflater.inflate(R.layout.activity_home, container, false);

        // Inisialisasi MapView
        map = view.findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Animasi teks per huruf - jalankan loop animasi
        TextView heroText = view.findViewById(R.id.hero_text);
        String fullText = getString(R.string.hero_text);
        animateTextPerCharacterLoop(heroText, fullText, 80, 2000);

        // Animasi scroll untuk fitur - panggil scheduleLayoutAnimation dengan post agar jalan
        LinearLayout featureContainer = view.findViewById(R.id.feature_container);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_slide_up);
        featureContainer.setLayoutAnimation(controller);
        featureContainer.post(() -> featureContainer.scheduleLayoutAnimation());

        // Minta izin lokasi
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        });

        return view;
    }

    private void setupMap() {
        if (map == null) return;

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);

        GeoPoint dpuprSolo = new GeoPoint(-7.5583975, 110.7900925);
        mapController.setCenter(dpuprSolo);

        Marker marker = new Marker(map);
        marker.setPosition(dpuprSolo);
        marker.setTitle("Dinas Pekerjaan Umum dan Penataan Ruang Surakarta");
        marker.setSubDescription("Jl. Belimbing No. 10, Surakarta");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        map.getOverlays().clear();
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        boolean permissionNeeded = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = true;
                break;
            }
        }
        if (permissionNeeded) {
            requestPermissions(permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            setupMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                setupMap();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();

        // Jika ingin restart animasi teks juga saat resume
        View view = getView();
        if (view != null) {
            TextView heroText = view.findViewById(R.id.hero_text);
            String fullText = getString(R.string.hero_text);
            animateTextPerCharacterLoop(heroText, fullText, 80, 2000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();

        // Hentikan animasi teks saat fragment pause agar tidak leak handler
        if (textAnimHandler != null && textAnimRunnable != null) {
            textAnimHandler.removeCallbacks(textAnimRunnable);
        }
    }

    // Animasi teks per huruf dengan looping
    private void animateTextPerCharacterLoop(TextView textView, String fullText, long delayMillis, long pauseAfter) {
        if (textAnimHandler == null) textAnimHandler = new Handler();

        textAnimRunnable = new Runnable() {
            int index = 0;
            final StringBuilder builder = new StringBuilder();

            @Override
            public void run() {
                if (index < fullText.length()) {
                    builder.append(fullText.charAt(index));
                    textView.setText(builder.toString());
                    index++;
                    textAnimHandler.postDelayed(this, delayMillis);
                } else {
                    // Setelah selesai, jeda sejenak lalu ulangi
                    textAnimHandler.postDelayed(() -> {
                        builder.setLength(0);
                        index = 0;
                        textView.setText("");
                        textAnimHandler.post(this);
                    }, pauseAfter);
                }
            }
        };

        // Reset dulu teks dan mulai animasi
        textView.setText("");
        textAnimHandler.post(textAnimRunnable);
    }
}

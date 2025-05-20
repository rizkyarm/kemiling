package com.android.kemilingcom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentMaps extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    public FragmentMaps() {
        // Required empty constructor
    }

    public static FragmentMaps newInstance(double lat, double lng) {
        FragmentMaps fragment = new FragmentMaps();
        Bundle args = new Bundle();
        args.putDouble("LATITUDE", lat);
        args.putDouble("LONGITUDE", lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            latitude = getArguments().getDouble("LATITUDE", 0.0);
            longitude = getArguments().getDouble("LONGITUDE", 0.0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnOpenMaps = view.findViewById(R.id.btn_openMaps);
        btnOpenMaps.setOnClickListener(v -> openGoogleMapsApp(latitude, longitude));


        Button btnSelesai = view.findViewById(R.id.btn_selesai);
        btnSelesai.setOnClickListener(v -> closeFragment());
    }

    private void openGoogleMapsApp(double lat, double lng) {
        String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Lokasi Produk)";
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Jika tidak ada Google Maps, buka dengan browser
            String browserUri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng productLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(productLocation).title("Lokasi Produk"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(productLocation, 15f));
    }

    private void closeFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();  // Menghapus fragment dari backstack

        // Sembunyikan container fragment agar tidak meninggalkan efek gelap
        requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
    }



}

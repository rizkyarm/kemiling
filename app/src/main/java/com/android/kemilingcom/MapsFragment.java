package com.android.kemilingcom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {

    private LocationSelectedListener listener;
    private GoogleMap googleMap;
    private LatLng selectedLatLng;
    private String contextType; // Parameter untuk menentukan konteks

    // Konstruktor baru dengan contextType parameter
    public MapsFragment(String contextType) {
        this.contextType = contextType;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap map) {
            googleMap = map;

            // Set default marker di Kemiling, Kota Bandar Lampung
            LatLng kemiling = new LatLng(-5.397139, 105.266792);
            googleMap.addMarker(new MarkerOptions().position(kemiling).title("Kemiling, Kota Bandar Lampung"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kemiling, 15));

            // Listener untuk memilih lokasi
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
                    selectedLatLng = latLng;
                    Button btnSelesai = getView().findViewById(R.id.btn_selesai);
                    btnSelesai.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LocationSelectedListener) {
            listener = (LocationSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LocationSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        Button btnSelesai = view.findViewById(R.id.btn_selesai);
        btnSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLatLng != null && listener != null) {
                    listener.onLocationSelected(selectedLatLng, contextType); // Pass contextType
                    getParentFragmentManager().popBackStack(); // Go back to the previous fragment
                } else {
                    // Show a message if no location is selected
                    Toast.makeText(getContext(), "Pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

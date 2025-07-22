package com.android.kemilingcom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FragmentMaps extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    // REVISI: Menambahkan variabel untuk koordinat origin (pengguna) dan destination (tujuan)
    private double originLat, originLng, destLat, destLng;

    // REVISI: Menambahkan RequestQueue untuk Volley
    private RequestQueue requestQueue;
    private static final String API_ROUTE_URL = "https://store.kemiling.com/api_get_route.php";

    public FragmentMaps() {
        // Required empty constructor
    }

    // REVISI: newInstance sekarang menerima 4 parameter (origin dan destination)
    public static FragmentMaps newInstance(double destLat, double destLng, double originLat, double originLng) {
        FragmentMaps fragment = new FragmentMaps();
        Bundle args = new Bundle();
        args.putDouble("DEST_LAT", destLat);
        args.putDouble("DEST_LNG", destLng);
        args.putDouble("ORIGIN_LAT", originLat);
        args.putDouble("ORIGIN_LNG", originLng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // REVISI: Inisialisasi RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // REVISI: Mengambil semua koordinat dari arguments
        if (getArguments() != null) {
            destLat = getArguments().getDouble("DEST_LAT", 0.0);
            destLng = getArguments().getDouble("DEST_LNG", 0.0);
            originLat = getArguments().getDouble("ORIGIN_LAT", 0.0);
            originLng = getArguments().getDouble("ORIGIN_LNG", 0.0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnOpenMaps = view.findViewById(R.id.btn_openMaps);
        btnOpenMaps.setOnClickListener(v -> openGoogleMapsApp(destLat, destLng));


        Button btnSelesai = view.findViewById(R.id.btn_selesai);
        btnSelesai.setOnClickListener(v -> closeFragment());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // REVISI: Menambahkan marker untuk origin dan destination, lalu atur kamera
        LatLng origin = new LatLng(originLat, originLng);
        LatLng destination = new LatLng(destLat, destLng);

        mMap.addMarker(new MarkerOptions().position(origin).title("Lokasi Anda"));
        mMap.addMarker(new MarkerOptions().position(destination).title("Lokasi Tujuan"));

        // Atur kamera agar menampilkan kedua titik
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        // 100 adalah padding dalam pixel
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

        // REVISI: Panggil fungsi untuk mengambil dan menggambar rute
        fetchAndDrawRoute();
    }

    // REVISI: Fungsi baru untuk mengambil data rute dari API dan menggambarnya
    private void fetchAndDrawRoute() {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("origin_lat", originLat);
            requestBody.put("origin_lng", originLng);
            requestBody.put("dest_lat", destLat);
            requestBody.put("dest_lng", destLng);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest routeRequest = new JsonObjectRequest(Request.Method.POST, API_ROUTE_URL, requestBody,
                response -> {
                    try {
                        if (isAdded() && response.getString("status").equals("success")) {
                            String encodedPolyline = response.getString("polyline");
                            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);

                            if (mMap != null) {
                                // Gambar garis rute di peta
                                mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(12).color(Color.BLUE));
                            }
                        } else {
                            Toast.makeText(getContext(), "Gagal mendapatkan data rute.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(isAdded()) {
                        Log.e("RouteAPI", "Error fetching route: " + error.toString());
                        Toast.makeText(getContext(), "Error: Gagal mengambil rute.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(routeRequest);
    }

    // Fungsi openGoogleMapsApp tidak berubah
    private void openGoogleMapsApp(double lat, double lng) {
        String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Lokasi Produk)";
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String browserUri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(browserUri));
            startActivity(browserIntent);
        }
    }

    // Fungsi closeFragment tidak berubah
    private void closeFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        }
    }
}
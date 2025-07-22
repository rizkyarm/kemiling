package com.android.kemilingcom;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class activity_product extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView btnBack;
    private TextView titleProduct, productPrice;
    private RequestQueue requestQueue;
    private MapView mapView;
    private GoogleMap googleMap;
    private double productLat = 0.0;
    private double productLng = 0.0;
    private ViewPager2 viewPager, viewPagerDescriptionMap;
    private ImageSliderAdapter imageSliderAdapter;
    private DescriptionMapSliderAdapter descriptionMapSliderAdapter;
    private String descriptionText;
    private Button btn_pesan,  btn_location;

    private static final String API_URL = "https://store.kemiling.com/api_product_detail.php?id=";
    private static final String API_ROUTE_URL = "https://store.kemiling.com/api_get_route.php";

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private double userLat = 0.0;
    private double userLng = 0.0;
    private boolean isMapPageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        viewPager = findViewById(R.id.view_pager);
        viewPagerDescriptionMap = findViewById(R.id.view_pager_description_map);
        titleProduct = findViewById(R.id.title_product);
        productPrice = findViewById(R.id.product_price);
        descriptionText = "Memuat deskripsi...";
        btnBack = findViewById(R.id.btn_back);
        btn_pesan = findViewById(R.id.btn_pesan);
        btn_location = findViewById(R.id.btn_location);

        mapView = new MapView(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);

        imageSliderAdapter = new ImageSliderAdapter(new ArrayList<>());
        viewPager.setAdapter(imageSliderAdapter);

        descriptionMapSliderAdapter = new DescriptionMapSliderAdapter();
        viewPagerDescriptionMap.setAdapter(descriptionMapSliderAdapter);
        descriptionMapSliderAdapter.addPage(descriptionText);
        descriptionMapSliderAdapter.addPage(mapView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PRODUCT_ID")) {
            int productId = intent.getIntExtra("PRODUCT_ID", -1);
            if (productId != -1) {
                fetchProductDetails(productId);
            }
        }

        getCurrentUserLocation();

        btnBack.setOnClickListener(v -> finish());
        btn_pesan.setOnClickListener(v -> {
            Intent checkoutIntent = new Intent(activity_product.this, CheckOut.class);
            checkoutIntent.putExtra("PRODUCT_ID", getIntent().getIntExtra("PRODUCT_ID", -1));
            checkoutIntent.putExtra("PRODUCT_TITLE", titleProduct.getText().toString());
            String priceText = productPrice.getText().toString().replace("Rp. ", "").replace(",", "");
            checkoutIntent.putExtra("PRODUCT_PRICE", priceText);
            checkoutIntent.putExtra("img_product", getIntent().getStringExtra("PRODUCT_IMAGE"));
            startActivity(checkoutIntent);
        });
        btn_location.setOnClickListener(v -> {
            if (isMapPageSelected) {
                // Jika peta sudah ditampilkan (klik kedua), buka Google Maps
                openGoogleMapsNavigation(productLat, productLng);
            } else {
                // Jika deskripsi yang ditampilkan (klik pertama), pindah ke peta
                viewPagerDescriptionMap.setCurrentItem(1);
            }
        });

        viewPagerDescriptionMap.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Jika halaman yang dipilih adalah peta (indeks 1), set flag ke true
                // Jika tidak, set ke false
                isMapPageSelected = (position == 1);
            }
        });
    }

    private void openGoogleMapsNavigation(double lat, double lng) {
        if (lat == 0.0 || lng == 0.0) {
            Toast.makeText(this, "Lokasi tujuan tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Aplikasi Google Maps tidak terpasang.", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchProductDetails(int productId) {
        String url = API_URL + productId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject product = response.getJSONObject("data");
                            titleProduct.setText(product.getString("title"));

                            // REVISI: Logika untuk menampilkan harga berdasarkan hari

                            // 1. Ambil semua jenis harga dari JSON menggunakan kunci yang benar
                            int price = product.optInt("price", 0);
                            int weekdayPrice = product.optInt("weekday_price", 0);
                            int weekendPrice = product.optInt("weekend_price", 0);

                            // 2. Dapatkan hari saat ini
                            Calendar calendar = Calendar.getInstance();
                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                            boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

                            int displayedPrice = 0;

                            // 3. Terapkan logika prioritas
                            if (isWeekend && weekendPrice > 0) {
                                displayedPrice = weekendPrice; // Prioritas utama di akhir pekan
                            } else if (!isWeekend && weekdayPrice > 0) {
                                displayedPrice = weekdayPrice; // Prioritas utama di hari kerja
                            } else if (price > 0) {
                                displayedPrice = price; // Gunakan harga umum jika tiket harian tidak ada
                            }

                            // 4. Tampilkan harga yang sudah ditentukan
                            if (displayedPrice > 0) {
                                productPrice.setText(String.format(Locale.US, "Rp. %,d", displayedPrice));
                            } else {
                                productPrice.setText("Harga Bervariasi");
                            }

                            // --- Akhir Revisi Harga ---

                            descriptionText = product.getString("description");
                            // Asumsi Anda punya TextView untuk deskripsi
                            TextView descriptionTextView = findViewById(R.id.description_text);
                            if(descriptionTextView != null) {
                                descriptionTextView.setText(descriptionText);
                            }

                            // Logika Gambar
                            JSONArray imagesArray = product.getJSONArray("images");
                            List<String> imageUrls = new ArrayList<>();
                            for (int i = 0; i < imagesArray.length(); i++) {
                                imageUrls.add(imagesArray.getString(i));
                            }
                            imageSliderAdapter.setImageUrls(imageUrls);
                            imageSliderAdapter.notifyDataSetChanged();

                            // Simpan koordinat produk
                            productLat = product.getDouble("latitude");
                            productLng = product.getDouble("longitude");

                            if (googleMap != null) {
                                updateMapAndRoute();
                            }

                        }
                    } catch (JSONException e) {
                        Log.e("ProductDetail", "Error parsing JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("ProductDetail", "Gagal mengambil data produk: " + error.getMessage())
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void updateMapAndRoute() {
        if (googleMap == null || productLat == 0.0 || userLat == 0.0) {
            return;
        }

        LatLng origin = new LatLng(userLat, userLng);
        LatLng destination = new LatLng(productLat, productLng);

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(origin).title("Lokasi Anda"));
        googleMap.addMarker(new MarkerOptions().position(destination).title("Lokasi Tujuan"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

        fetchRouteAndDraw(origin, destination);
    }

    private void fetchRouteAndDraw(LatLng origin, LatLng destination) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("origin_lat", origin.latitude);
            requestBody.put("origin_lng", origin.longitude);
            requestBody.put("dest_lat", destination.latitude);
            requestBody.put("dest_lng", destination.longitude);
        } catch (JSONException e) { e.printStackTrace(); return; }

        JsonObjectRequest routeRequest = new JsonObjectRequest(Request.Method.POST, API_ROUTE_URL, requestBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            String encodedPolyline = response.getString("polyline");
                            List<LatLng> decodedPath = PolyUtil.decode(encodedPolyline);
                            if (googleMap != null) {
                                googleMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(12).color(Color.BLUE));
                            }
                        } else {
                            // REVISI 2: Tambahkan Toast untuk notifikasi error dari API
                            String message = response.optString("message", "Gagal mendapatkan info rute");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> {
                    // REVISI 2: Tambahkan Toast untuk notifikasi error jaringan
                    Log.e("RouteAPI", "Error: " + error.toString());
                    Toast.makeText(this, "Error: Tidak dapat terhubung ke server rute.", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(routeRequest);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        updateMapAndRoute();
    }

    private void getCurrentUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLat = location.getLatitude();
                    userLng = location.getLongitude();
                    updateMapAndRoute();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentUserLocation();
            } else {
                Toast.makeText(this, "Izin lokasi dibutuhkan.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Metode lifecycle MapView
    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
}
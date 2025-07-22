package com.android.kemilingcom;

import static com.android.kemilingcom.Db_Contract.url_nearby;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class activity_terdekat extends AppCompatActivity {

    private TextView userProfileTextView;
    private TextView locationTextView;
    private RecyclerView nearbyRecyclerView;
    private productAdapter adapter;
    private List<product> productList;
    private BottomNavigationView bottomNavigationView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double currentLatitude = 0, currentLongitude = 0;

    private RequestQueue requestQueue;
    private boolean isActivityDestroyed = false; // Flag untuk track status activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terdekat);

        // Inisialisasi request queue
        requestQueue = Volley.newRequestQueue(this);

        // Inisialisasi UI
        initializeViews();

        // Setup navigation
        setupBottomNavigation();

        // Setup user profile
        setupUserProfile();

        // Setup location dan RecyclerView
        setupLocationAndRecyclerView();
    }

    private void initializeViews() {
        userProfileTextView = findViewById(R.id.user_profile);
        locationTextView = findViewById(R.id.my_location);
        nearbyRecyclerView = findViewById(R.id.recycleview_product);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.menu_near);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (isActivityDestroyed || isFinishing()) {
                    return false;
                }

                switch (item.getItemId()) {
                    case R.id.menu_home:
                        navigateToActivity(activity_beranda.class);
                        return true;
                    case R.id.menu_near:
                        return true;
                    case R.id.menu_akun:
                        navigateToActivity(activity_account.class);
                        return true;
                }
                return false;
            }
        });
    }

    private void navigateToActivity(Class<?> targetActivity) {
        try {
            startActivity(new Intent(this, targetActivity));
            overridePendingTransition(0, 0);
            finish();
        } catch (Exception e) {
            Log.e("Navigation Error", "Error navigating to activity: " + e.getMessage());
        }
    }

    private void setupUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        userProfileTextView.setText("Hi, " + userName);
    }

    private void setupLocationAndRecyclerView() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        productList = new ArrayList<>();
        adapter = new productAdapter(this, productList);
        nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyRecyclerView.setAdapter(adapter);
        getDeviceLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
        if (adapter != null) {
            adapter.cleanup();
        }
        if (productList != null) {
            productList.clear();
        }
        Log.d("ActivityLifecycle", "activity_terdekat destroyed and cleaned up");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (requestQueue != null) {
            requestQueue.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestQueue != null) {
            requestQueue.start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void getDeviceLocation() {
        if (isActivityDestroyed || isFinishing()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (isActivityDestroyed || isFinishing() || location == null) {
                            if(location == null) safeSetText(locationTextView, "Lokasi tidak tersedia");
                            return;
                        }
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        getAddressFromLocation(currentLatitude, currentLongitude);
                        fetchNearbyProducts();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Location Error", "Failed to get location: " + e.getMessage());
                        safeSetText(locationTextView, "Gagal mendapatkan lokasi");
                    });
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        if (isActivityDestroyed || isFinishing()) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        new Thread(() -> {
            try {
                if (isActivityDestroyed || isFinishing()) return;
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressText = formatAddress(addresses.get(0));
                    runOnUiThread(() -> safeSetText(locationTextView, addressText));
                } else {
                    runOnUiThread(() -> safeSetText(locationTextView, "Lokasi tidak dikenali"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> safeSetText(locationTextView, "Gagal mendapatkan alamat"));
            }
        }).start();
    }

    private void safeSetText(TextView textView, String text) {
        if (textView != null && !isActivityDestroyed && !isFinishing()) {
            textView.setText(text);
        }
    }

    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getThoroughfare() != null) sb.append(address.getThoroughfare()).append(", ");
        if (address.getSubLocality() != null) sb.append(address.getSubLocality()).append(", ");
        if (address.getLocality() != null) sb.append(address.getLocality()).append(", ");
        if (address.getAdminArea() != null) sb.append(address.getAdminArea());
        if (sb.length() > 0 && sb.charAt(sb.length() - 2) == ',') {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    private void fetchNearbyProducts() {
        if (isActivityDestroyed || isFinishing() || currentLatitude == 0 || currentLongitude == 0) {
            Log.e("Location Error", "Lokasi tidak valid. Tidak bisa mengambil data produk.");
            return;
        }

        String url = url_nearby;
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", currentLatitude);
            requestBody.put("longitude", currentLongitude);
        } catch (JSONException e) {
            Log.e("JSON Error", e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    if (isActivityDestroyed || isFinishing()) {
                        return;
                    }
                    try {
                        if (!response.getString("status").equals("success")) {
                            Log.e("API Response", "Full Response: " + response.toString());
                            return;
                        }
                        JSONArray dataArray = response.getJSONArray("data");
                        productList.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            if (isActivityDestroyed || isFinishing()) break;

                            JSONObject productObject = dataArray.getJSONObject(i);

                            int id = productObject.getInt("id_produk");
                            String title = productObject.getString("nama_produk");
                            String category = productObject.optString("kategori", "UMKM");
                            String imageUrl = productObject.optString("image", "");
                            int price = productObject.optInt("price", 0);
                            int weekday_ticket = productObject.optInt("weekday_ticket", 0);
                            int weekend_ticket = productObject.optInt("weekend_ticket", 0);
                            String location = productObject.optString("kategori", "Tidak ada lokasi");
                            float rating = (float) productObject.optDouble("rating", 0.0);

                            // REVISI 1: Ambil data 'distance_route_km' dari JSON
                            double distance = productObject.optDouble("distance_route_km", 0.0);

                            // REVISI 2: Masukkan variabel 'distance' ke dalam constructor product
                            productList.add(new product(id, title, price, weekday_ticket, weekend_ticket, category, location, rating, imageUrl, distance));
                        }

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Parsing Error", e.getMessage());
                    }
                },
                error -> {
                    if (isActivityDestroyed || isFinishing()) {
                        return;
                    }
                    Log.e("Volley Request Error", error.toString());
                    Toast.makeText(activity_terdekat.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setTag(this);
        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                safeSetText(locationTextView, "Izin lokasi ditolak");
                Toast.makeText(this, "Izin lokasi diperlukan untuk menampilkan produk terdekat", Toast.LENGTH_LONG).show();
            }
        }
    }
}
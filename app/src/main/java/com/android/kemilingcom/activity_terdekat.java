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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terdekat);

        // Inisialisasi UI
        userProfileTextView = findViewById(R.id.user_profile);
        locationTextView = findViewById(R.id.my_location);
        nearbyRecyclerView = findViewById(R.id.recycleview_product);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Navigasi Bawah
        bottomNavigationView.setSelectedItemId(R.id.menu_near);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        startActivity(new Intent(activity_terdekat.this, activity_beranda.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.menu_near:
                        return true;
                    case R.id.menu_akun:
                        startActivity(new Intent(activity_terdekat.this, activity_account.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                }
                return false;
            }
        });



        // Menampilkan nama user
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        userProfileTextView.setText("Hi, " + userName);

        // Inisialisasi lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getDeviceLocation();

        // RecyclerView
        productList = new ArrayList<>();
        adapter = new productAdapter(this, productList);
        nearbyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyRecyclerView.setAdapter(adapter);
    }
    // Override onBackPressed()
    @Override
    public void onBackPressed() {
        // Finish the activity to exit the app
        super.onBackPressed();
        finishAffinity();
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            // Convert koordinat ke nama lokasi
                            getAddressFromLocation(currentLatitude, currentLongitude);

                            fetchNearbyProducts();
                        } else {
                            locationTextView.setText("Lokasi tidak tersedia");
                        }
                    });
        }
    }

    // Method untuk reverse geocoding
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = formatAddress(address);

                    runOnUiThread(() ->
                            locationTextView.setText(addressText)
                    );
                } else {
                    runOnUiThread(() ->
                            locationTextView.setText("Lokasi tidak dikenali")
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        locationTextView.setText("Gagal mendapatkan alamat")
                );
            }
        }).start();
    }

    // Format alamat menjadi lebih rapi
    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        if (address.getThoroughfare() != null) sb.append(address.getThoroughfare()).append(", ");
        if (address.getSubLocality() != null) sb.append(address.getSubLocality()).append(", ");
        if (address.getLocality() != null) sb.append(address.getLocality()).append(", ");
        if (address.getAdminArea() != null) sb.append(address.getAdminArea());

        // Hapus koma di akhir jika ada
        if (sb.length() > 0 && sb.charAt(sb.length()-2) == ',') {
            sb.setLength(sb.length()-2);
        }

        return sb.toString();
    }

    private void fetchNearbyProducts() {
        if (currentLatitude == 0 || currentLongitude == 0) {
            Log.e("Location Error", "Lokasi tidak tersedia. Tidak bisa mengambil data produk.");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = url_nearby;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("latitude", currentLatitude);
            requestBody.put("longitude", currentLongitude);
        } catch (JSONException e) {
            Log.e("",e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getString("status").equals("success")) {
                                Log.e("API Response", "Full Response: " + response.toString());
                                return;
                            }

                            JSONArray dataArray = response.getJSONArray("data");
                            productList.clear();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject productObject = dataArray.getJSONObject(i);
                                int id = productObject.getInt("id_produk");
                                String title = productObject.getString("nama_produk");
                                int price = productObject.getInt("price");
                                String location = productObject.getString("kategori");
                                float rating = (float) productObject.optDouble("rating", 0.0);
                                String imageUrl = productObject.optString("image", "");
                                productList.add(new product(id, title, price, location, rating, imageUrl));
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("JSON Parsing Error", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Request Error", error.toString());
                        Toast.makeText(activity_terdekat.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setShouldCache(false);
        queue.add(jsonObjectRequest);
    }
}

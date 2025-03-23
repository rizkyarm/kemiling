package com.android.kemilingcom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        mapView = new MapView(this);
        productPrice = findViewById(R.id.product_price);
        descriptionText = "Deskripsi produk akan ditampilkan di sini.";
        btnBack = findViewById(R.id.btn_back);

        btn_pesan = findViewById(R.id.btn_pesan);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set up ViewPager2 adapters
        imageSliderAdapter = new ImageSliderAdapter(new ArrayList<>());
        viewPager.setAdapter(imageSliderAdapter);

        descriptionMapSliderAdapter = new DescriptionMapSliderAdapter();
        viewPagerDescriptionMap.setAdapter(descriptionMapSliderAdapter);

        // Add description and map to the adapter
        descriptionMapSliderAdapter.addPage(descriptionText);
        descriptionMapSliderAdapter.addPage(mapView);

        // Get PRODUCT_ID from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PRODUCT_ID")) {
            int productId = intent.getIntExtra("PRODUCT_ID", -1);
            if (productId != -1) {
                fetchProductDetails(productId);
            } else {
                Log.e("ProductDetail", "ID Produk tidak valid.");
            }
        } else {
            Log.e("ProductDetail", "PRODUCT_ID tidak ditemukan dalam intent.");
        }
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_pesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_product.this, CheckOut.class);

                // Kirim data produk ke Checkout
                intent.putExtra("PRODUCT_ID", getIntent().getIntExtra("PRODUCT_ID", -1));
                intent.putExtra("PRODUCT_TITLE", titleProduct.getText().toString());
                intent.putExtra("PRODUCT_PRICE", productPrice.getText().toString());
                intent.putExtra("img_product", getIntent().getStringExtra("img_product"));

                startActivity(intent);
            }
        });

        btn_location = findViewById(R.id.btn_location);
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapFragment();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

                if (fragment instanceof FragmentMaps) {
                    // Jika MapsFragment sedang ditampilkan, tutup fragment
                    fragmentManager.popBackStack();
                    findViewById(R.id.fragment_container).setVisibility(View.GONE);
                } else {
                    // Jika tidak ada fragment yang ditampilkan, lakukan back seperti biasa
                    finish();
                }
            }
        });


    }

    private void showMapFragment() {
        FragmentMaps mapsFragment = FragmentMaps.newInstance(productLat, productLng);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_container, mapsFragment);
        transaction.addToBackStack(null);  // Tambahkan ke BackStack agar bisa dihapus nanti
        transaction.commit();

        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
    }






    private void fetchProductDetails(int productId) {
        String url = API_URL + productId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                JSONObject product = response.getJSONObject("data");

                                titleProduct.setText(product.getString("title"));

                                // Check if "prices" object exists
                                if (product.has("prices")) {
                                    JSONObject prices = product.getJSONObject("prices");

                                    // Retrieve prices, handling potential absence
                                    int weekdayPrice = prices.optInt("weekday_price", 0); // Default to 0 if not found
                                    int weekendPrice = prices.optInt("weekend_price", 0); // Default to 0 if not found

                                    // Format and display the prices
                                    String formattedWeekdayPrice = "Rp. " + String.format("%,d", weekdayPrice);
                                    String formattedWeekendPrice = "Rp. " + String.format("%,d", weekendPrice);

                                    // Display the prices
                                    productPrice.setText("Weekday: " + formattedWeekdayPrice + "\nWeekend: " + formattedWeekendPrice);
                                } else {
                                    // If "prices" object is not present, try to get "weekday_price" directly
                                    if (product.has("weekday_price") && !product.isNull("weekday_price")) {
                                        int weekdayPrice = product.getInt("weekday_price");
                                        String formattedWeekdayPrice = "Rp. " + String.format("%,d", weekdayPrice);
                                        productPrice.setText(formattedWeekdayPrice);
                                    } else {
                                        productPrice.setText("Price not available");
                                    }
                                }

                                // Update description
                                descriptionText = product.getString("description");
                                descriptionMapSliderAdapter.updateDescription(descriptionText);

                                // Load multiple images into ViewPager2
                                JSONArray imagesArray = product.getJSONArray("images");
                                List<String> imageUrls = new ArrayList<>();

                                for (int i = 0; i < imagesArray.length(); i++) {
                                    String imageUrl = imagesArray.getString(i);
                                    imageUrls.add(imageUrl);

                                    // Logging URL of each image
                                    Log.d("Image URL", imageUrl);
                                    // Test the URL by trying to load it directly
                                    new Thread(() -> {
                                        try {
                                            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                                            connection.setRequestMethod("GET");
                                            connection.connect();
                                            int responseCode = connection.getResponseCode();
                                            Log.d("URL Test", "URL: " + imageUrl + " Response Code: " + responseCode);
                                        } catch (IOException e) {
                                            Log.e("URL Test", "Failed to load URL: " + imageUrl, e);
                                        }
                                    }).start();
                                }

                                imageSliderAdapter.setImageUrls(imageUrls);
                                imageSliderAdapter.notifyDataSetChanged();

                                // Retrieve latitude and longitude
                                productLat = product.getDouble("latitude");
                                productLng = product.getDouble("longitude");

                                // Update map with product location
                                updateMap();
                            } else {
                                Log.e("ProductDetail", "Produk tidak ditemukan.");
                            }
                        } catch (JSONException e) {
                            Log.e("ProductDetail", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ProductDetail", "Gagal mengambil data produk: " + error.getMessage());
                    }
                }
        );

        // Add request to queue
        requestQueue.add(jsonObjectRequest);
    }





    private void updateMap() {
        if (googleMap != null && productLat != 0.0 && productLng != 0.0) {
            LatLng productLocation = new LatLng(productLat, productLng);
            googleMap.clear(); // Clear previous markers
            googleMap.addMarker(new MarkerOptions()
                    .position(productLocation)
                    .title("Lokasi Produk"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(productLocation, 15f)); // Zoom to location
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        updateMap(); // Update map when GoogleMap is ready
    }

    // Lifecycle methods for MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

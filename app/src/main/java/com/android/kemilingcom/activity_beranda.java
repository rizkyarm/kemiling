package com.android.kemilingcom;

import static com.android.kemilingcom.Db_Contract.url_search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class activity_beranda extends AppCompatActivity {

    private TextView userProfileTextView;
    private EditText et_search;
    private LinearLayout btn_wisata, btn_umkm;
    private RecyclerView recommend; // RecyclerView untuk menampilkan rekomendasi produk
    private productAdapter adapter; // Adapter untuk RecyclerView
    private List<product> productList; // List untuk menyimpan data produk
    private BottomNavigationView bottomNavigationView; // Bottom Navigation

    private static final String BASE_URL = "https://store.kemiling.com/";
    private static final String url_image_profile = BASE_URL + "api_image_profile.php";

    private ImageView profileImageView;
    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RequestQueue requestQueue;


    private int offset = 0;
    private final int limit = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String selectedCategory = null; // Simpan kategori yang sedang digunakan


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable tampilan edge-to-edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        // Mengatur padding sesuai sistem insets (status bar, navigation bar, dll)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestQueue = Volley.newRequestQueue(this);


        // Inisialisasi komponen UI
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        userProfileTextView = findViewById(R.id.user_profile);
        et_search = findViewById(R.id.edit_search);
        btn_wisata = findViewById(R.id.wisata_btn);
        btn_umkm = findViewById(R.id.umkm_btn);
        recommend = findViewById(R.id.recyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // Inisialisasi BottomNavigationView
        profileImageView = findViewById(R.id.image_profile); // Inisialisasi ImageView



        // Konfigurasi BottomNavigationView
        bottomNavigationView.setSelectedItemId(R.id.menu_home); // Set menu Home sebagai default
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        // Sudah berada di halaman Home (activity_beranda)
                        return true;
                    case R.id.menu_near:
                        intent = new Intent(activity_beranda.this, activity_terdekat.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish(); // Finish current activity
                        return true;
                    case R.id.menu_akun:
                        intent = new Intent(activity_beranda.this, activity_account.class); // Assuming you have activity_akun
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish(); // Finish current activity
                        return true;
                }
                return false;
            }
        });

        // Ambil nama pengguna dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        // Tampilkan nama pengguna
        userProfileTextView.setText("Hi, " + userName);

        String apiUrl = url_image_profile + "?username=" + userName;

        // Fetch and set the profile image
        fetchProfileImage(apiUrl, profileImageView);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        adapter = new productAdapter(this, productList, false);  // false for activity_beranda
        recyclerView.setAdapter(adapter);
        recommend.setLayoutManager(new LinearLayoutManager(this));
        recommend.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshData();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0) {
                        fetchProducts(null); // Gunakan selectedCategory yang tersimpan
                    }
                }
            }
        });


        // Pull-to-refresh untuk merefresh semua elemen dalam activity
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
        });

        // Listener untuk tombol Wisata
        btn_wisata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList.clear(); // Kosongkan daftar produk sebelum fetch
                adapter.notifyDataSetChanged();
                fetchProducts("Tempat Wisata");
            }
        });

        // Listener untuk tombol UMKM
        btn_umkm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productList.clear(); // Kosongkan daftar produk sebelum fetch
                adapter.notifyDataSetChanged();
                fetchProducts("UMKM");
            }
        });

        // Ambil data produk default (semua produk)
        fetchProducts(null);

        // Event listener untuk pencarian produk secara real-time
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Tidak perlu melakukan apa pun sebelum teks diubah
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                searchProducts(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Tidak perlu melakukan apa pun setelah teks diubah
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(tag -> true); // cancel semua request
        }
    }


    private void refreshData() {
        // 1. Ambil ulang nama pengguna
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        userProfileTextView.setText("Hi, " + userName);

        // 2. Ambil ulang foto profil
        String apiUrl = BASE_URL + "api_image_profile.php?username=" + userName;
        fetchProfileImage(apiUrl, profileImageView);

        // 3. Kosongkan pencarian
        et_search.setText("");

        // 4. Ambil ulang daftar produk
        fetchProducts(null);

        // 5. Hentikan animasi refresh setelah semua selesai (delay untuk efek visual)
        new Handler().postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
        }, 2000);
    }


    private void fetchProfileImage(String apiUrl, ImageView imageView) {
        // Fetch image URL from API
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("status").equals("success")) {
                        String imageUrl = jsonObject.getString("profile_image_url");
                        String fullImageUrl = BASE_URL + imageUrl;
                        // Load image using Glide
                        Glide.with(activity_beranda.this)
                                .load(fullImageUrl)
                                .into(profileImageView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        // Finish the activity to exit the app
        super.onBackPressed();
        moveTaskToBack(false);
    }

    private void fetchProducts(String category) {
        if (isLoading || isLastPage) return;

        isLoading = true;

        // Simpan kategori hanya jika offset == 0 (panggilan pertama)
        if (offset == 0) {
            selectedCategory = category;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url;

        // Gunakan selectedCategory, bukan parameter langsung
        if (selectedCategory == null) {
            url = Db_Contract.url_product;
        } else if (selectedCategory.equals("Tempat Wisata")) {
            url = Db_Contract.url_product_wisata;
        } else if (selectedCategory.equals("UMKM")) {
            url = Db_Contract.url_product_umkm;
        } else {
            url = Db_Contract.url_product;
        }

        // Tambahkan offset & limit
        url += "?offset=" + offset + "&limit=" + limit;

        Log.d("FetchProducts", "Fetching from URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");

                        if (dataArray.length() == 0) {
                            isLastPage = true;
                            return;
                        }

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject productObject = dataArray.getJSONObject(i);

                            int id = productObject.getInt("id");
                            String title = productObject.getString("title");
                            int price = productObject.optInt("price", 0);
                            int weekday_ticket = productObject.optInt("weekday_ticket", 0);
                            int weekend_ticket = productObject.optInt("weekend_ticket", 0);
                            String categoryName = productObject.optString("category", "UMKM");
                            String location = productObject.getString("location");
                            float rating = (float) productObject.getDouble("rating");
                            String imageUrl = productObject.getString("image_url");

                            productList.add(new product(id, title, price, weekday_ticket, weekend_ticket, categoryName, location, rating, imageUrl));
                        }

                        offset += dataArray.length();
                        adapter.notifyDataSetChanged();
                        isLoading = false;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        isLoading = false;
                    }
                },
                error -> {
                    error.printStackTrace();
                    isLoading = false;
                }
        );

        jsonObjectRequest.setShouldCache(false);
        queue.add(jsonObjectRequest);
    }



    private void searchProducts(String query) {
        if (query.isEmpty()) {
            fetchProducts(null); // Jika query kosong, tampilkan semua produk
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = url_search + "?query=" + query; // Kirim query ke API

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
                                JSONArray dataArray = response.getJSONArray("data");

                                // Kosongkan daftar produk sebelum menambah hasil pencarian
                                productList.clear();

                                // Parsing JSON dan menambahkan produk ke daftar
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject productObject = dataArray.getJSONObject(i);

                                    int id = productObject.getInt("id");
                                    String title = productObject.getString("title");
                                    int price = productObject.getInt("price");

                                    // Ambil weekday_ticket & weekend_ticket (gunakan default jika tidak ada)
                                    int weekday_ticket = productObject.has("weekday_ticket") ?
                                            productObject.getInt("weekday_ticket") : 0;
                                    int weekend_ticket = productObject.has("weekend_ticket") ?
                                            productObject.getInt("weekend_ticket") : 0;

                                    String category = productObject.has("category") ?
                                            productObject.getString("category") : "UMKM"; // Default ke UMKM

                                    String location = productObject.getString("location");
                                    float rating = (float) productObject.getDouble("rating");
                                    String imageUrl = productObject.getString("image_url");

                                    // Tambahkan produk ke list dengan category, weekday_ticket, dan weekend_ticket
                                    productList.add(new product(id, title, price, weekday_ticket, weekend_ticket, category, location, rating, imageUrl));
                                }

                                adapter.notifyDataSetChanged();
                            } else {
                                productList.clear();
                                adapter.notifyDataSetChanged();
                                System.out.println("Tidak ada produk yang ditemukan.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        System.out.println("Error dalam request: " + error.getMessage());
                    }
                }
        );

        // Tambahkan request ke antrian
        queue.add(jsonObjectRequest);
    }


}

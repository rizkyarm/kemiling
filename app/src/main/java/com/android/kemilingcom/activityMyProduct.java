package com.android.kemilingcom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class activityMyProduct extends AppCompatActivity {
    private static final String TAG = "activityMyProduct";
    private List<product> productlist;
    private productAdapterEdit adapter;
    private RecyclerView recyclerView;
    private Button addProduct, addProductUmkm;
    private ImageView btnBack;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        addProduct = findViewById(R.id.btn_tambah_product);
        addProductUmkm = findViewById(R.id.btn_tambah_product_umkm);
        recyclerView = findViewById(R.id.productlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btn_back2);


        productlist = new ArrayList<>();
        adapter = new productAdapterEdit(this, productlist, true);  // true for activityMyProduct
        recyclerView.setAdapter(adapter);

        // Fetch user ID from server and store it in SharedPreferences, then fetch products
        fetchAndStoreUserId();

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityMyProduct.this , addProductWisata.class);
                startActivity(intent);
            }
        });

        addProductUmkm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activityMyProduct.this , addProductUmkm.class);
                startActivity(intent);
            }
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    private void fetchAndStoreUserId() {
        String url = "https://store.kemiling.com/api_endpoint.php";
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        if (userName.isEmpty()) {
            Toast.makeText(this, "Username is not available in SharedPreferences", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        int userId = jsonResponse.getInt("user_id");

                        // Store user_id in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("user_id", userId);
                        editor.apply();

                        Toast.makeText(activityMyProduct.this, "User ID: " + userId, Toast.LENGTH_SHORT).show();

                        // Now fetch products
                        fetchProducts(userId);
                    } else {
                        Toast.makeText(activityMyProduct.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error fetching user ID", error);
                Toast.makeText(activityMyProduct.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", userName);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void fetchProducts(int userId) {
        String url = "https://store.kemiling.com/api_myproduct.php?user_id=" + userId;

        Log.d("FetchProducts", "Fetching from URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray dataArray = response.getJSONArray("data");
                            productlist.clear(); // Kosongkan data sebelumnya

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject productObject = dataArray.getJSONObject(i);

                                int id = productObject.getInt("id");
                                String title = productObject.getString("title");
                                int price = productObject.getInt("price");

                                // Ambil category, default ke UMKM jika tidak ada
                                String category = productObject.has("category") ?
                                        productObject.getString("category") : "UMKM";

                                // Ambil weekday_ticket & weekend_ticket, default ke 0 jika tidak ada
                                int weekday_ticket = productObject.has("weekday_ticket") ?
                                        productObject.getInt("weekday_ticket") : 0;
                                int weekend_ticket = productObject.has("weekend_ticket") ?
                                        productObject.getInt("weekend_ticket") : 0;

                                String location = productObject.getString("location");
                                float rating = (float) productObject.getDouble("rating");
                                String imageUrl = productObject.getString("image_url");

                                // Tambahkan ke list produk dengan category dan ticket prices
                                productlist.add(new product(id, title, price, weekday_ticket, weekend_ticket, category, location, rating, imageUrl));
                            }

                            adapter.notifyDataSetChanged(); // Perbarui RecyclerView
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSON Parsing Error", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("Volley Request Error", error.getMessage());
                    }
                }
        );

        jsonObjectRequest.setShouldCache(false);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }


}

package com.android.kemilingcom;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class addProductUmkm extends AppCompatActivity implements LocationSelectedListener{
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editproduk, editharga, editJam, editAlamat, editWA, editDeskripsi;
    private Button btnPilihMap, btnSubmit;
    private ImageView btnBack, imageHeader;
    private ImageView[] imageViews = new ImageView[5];
    private Bitmap headerBitmap;
    private Bitmap[] imageBitmaps = new Bitmap[5];
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product_umkm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize elements
        editproduk = findViewById(R.id.editproduk);
        editharga = findViewById(R.id.editharga);
        editJam = findViewById(R.id.editjam);
        editAlamat = findViewById(R.id.editalamat);
        editWA = findViewById(R.id.editWA);
        editDeskripsi = findViewById(R.id.editdeskripsi);
        btnPilihMap = findViewById(R.id.btn_pilih_map);
        btnBack = findViewById(R.id.btn_back3);
        imageHeader = findViewById(R.id.imageHeader);
        btnSubmit = findViewById(R.id.btnUploadAll);

        imageViews[0] = findViewById(R.id.image1);
        imageViews[1] = findViewById(R.id.image2);
        imageViews[2] = findViewById(R.id.image3);
        imageViews[3] = findViewById(R.id.image4);
        imageViews[4] = findViewById(R.id.image5);

        // Back to the previous page
        btnBack.setOnClickListener(view -> finish());

        // Open maps fragment when button is clicked
        btnPilihMap.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            MapsFragment mapsFragment = new MapsFragment("addProductUmkm");
            fragmentManager.beginTransaction()
                    .replace(R.id.main, mapsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnSubmit.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", 0);

            if (userId == 0) {
                fetchAndStoreUserId();
            } else {
                sendData(userId);
            }
        });


        // Select header image
        imageHeader.setOnClickListener(v -> chooseImage(0));

        // Select additional images
        for (int i = 0; i < imageViews.length; i++) {
            final int index = i;
            imageViews[i].setOnClickListener(v -> chooseImage(index + 1));
        }
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
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

                        Toast.makeText(addProductUmkm.this, "User ID: " + userId, Toast.LENGTH_SHORT).show();

                        sendData(userId);
                    } else {
                        Toast.makeText(addProductUmkm.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error fetching user ID", error);
                Toast.makeText(addProductUmkm.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
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



    public void useFragmentMaps(String contextType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapsFragment mapsFragment = new MapsFragment(contextType);
        fragmentManager.beginTransaction()
                .replace(R.id.main, mapsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLocationSelected(LatLng latLng, String contextType) {
        if (contextType.equals("addProductUmkm")) {
            // Handle location for addProductUmkm
            latitude = latLng.latitude;
            longitude = latLng.longitude;
            String location = "Lat: " + latitude + ", Lng: " + longitude;
            Toast.makeText(this, "Location Selected: " + location, Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImage(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST + requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                if (requestCode == PICK_IMAGE_REQUEST) {
                    headerBitmap = bitmap;
                    imageHeader.setImageBitmap(bitmap);
                } else if (requestCode >= PICK_IMAGE_REQUEST + 1 && requestCode <= PICK_IMAGE_REQUEST + 5) {
                    int index = requestCode - PICK_IMAGE_REQUEST - 1;
                    imageBitmaps[index] = bitmap;
                    imageViews[index].setImageBitmap(bitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendData(int userId) {
        String produk = editproduk.getText().toString().trim();
        String harga = editharga.getText().toString().trim();
        String jam = editJam.getText().toString().trim();
        String alamat = editAlamat.getText().toString().trim();
        String wa = editWA.getText().toString().trim();
        String deskripsi = editDeskripsi.getText().toString().trim();

        if (produk.isEmpty() || harga.isEmpty() || jam.isEmpty() || alamat.isEmpty() || wa.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(addProductUmkm.this, "Harap isi semua bidang", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://store.kemiling.com/side-page/Pendaftaran/api_addProductUmkm.php?user_id=" + userId;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nama_produk", produk);
            jsonObject.put("kategori", "UMKM");
            jsonObject.put("harga_produk", harga);
            jsonObject.put("jam_operasional", jam);
            jsonObject.put("alamat", alamat);
            jsonObject.put("no_wa", wa);
            jsonObject.put("deskripsi", deskripsi);

            if (headerBitmap != null) {
                String base64Header = bitmapToBase64(headerBitmap);
                jsonObject.put("image_header", base64Header);
                Log.d("ImageBase64", "Header Image: " + base64Header); // Logging Base64 Image
            }

            JSONArray imagesArray = new JSONArray();
            for (Bitmap bitmap : imageBitmaps) {
                if (bitmap != null) {
                    String base64Image = bitmapToBase64(bitmap);
                    imagesArray.put(base64Image);
                    Log.d("ImageBase64", "Array Image: " + base64Image); // Logging Base64 Image
                }
            }
            jsonObject.put("images", imagesArray);
            jsonObject.put("lat", latitude);
            jsonObject.put("lng", longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("JSON Data", jsonObject.toString()); // Logging JSON Data before sending

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Server Response", response); // Logging Server Response
                Toast.makeText(addProductUmkm.this, "Data berhasil dikirim", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(addProductUmkm.this, activityMyProduct.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Upload Error", error.getMessage()); // Logging Error Response
                Toast.makeText(addProductUmkm.this, "Gagal mengirim data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public byte[] getBody() {
                return jsonObject.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(request);
    }


    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
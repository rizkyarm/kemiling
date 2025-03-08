package com.android.kemilingcom;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class CheckOut extends AppCompatActivity {

    private TextView txtUsaha, textNamaProduct, textHarga, editNamaCs, txtRek1, txtRek2, txtRek3, NamaCS, totalharga, rek1, rek2, rek3;
    ;
    private EditText editJumlahTiket;
    private Button btnBayar, btn_upload_bukti, btnPaid, btnnotf;
    private RequestQueue requestQueue;
    private ImageView imgProduct, imgBukti, btnback;
    private Uri imageUri;
    private String buktiTf;

    private int userId, idProduk, weekdayTicket, weekendTicket, hargaProduk;
    private String usernameSeller, namaProduk, usernameBuyer;
    private static final String API_PRODUK = "https://store.kemiling.com/api_product_detail.php?id=";
    private static final String API_REKENING = "https://store.kemiling.com/api_rekening.php";
    private static final String API_PEMESANAN = "https://store.kemiling.com/api_pembayaran.php";
    private int price;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);


        // Setup window insets
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            return windowInsets;
        });
        requestQueue = Volley.newRequestQueue(this);

        // Ambil nama pengguna dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        String productTitle = getIntent().getStringExtra("PRODUCT_TITLE");
        String productPrice = getIntent().getStringExtra("PRODUCT_PRICE");


        // Inisialisasi View
        imgProduct = findViewById(R.id.img_product);
        imgBukti = findViewById(R.id.img_bukti);
        txtUsaha = findViewById(R.id.txt_usaha);
        textNamaProduct = findViewById(R.id.text_namaproduct);
        textHarga = findViewById(R.id.text_harga);
        totalharga = findViewById(R.id.total_harga);
        txtRek1 = findViewById(R.id.txt_rek1);
        txtRek2 = findViewById(R.id.txt_rek2);
        txtRek3 = findViewById(R.id.txt_rek3);
        rek1 = findViewById(R.id.rek1);
        rek2 = findViewById(R.id.rek2);
        rek3 = findViewById(R.id.rek3);
        NamaCS = findViewById(R.id.edit_namacs);
        String userIdCs = "";
        editJumlahTiket = findViewById(R.id.edit_jumlahtkt);
        btnBayar = findViewById(R.id.btn_paid);
        btn_upload_bukti = findViewById(R.id.btn_upload_bukti);
        btnPaid = findViewById(R.id.btn_paid);
        btnback = findViewById(R.id.btn_back5);
        btnnotf = findViewById(R.id.btn_notf);
        requestQueue = Volley.newRequestQueue(this);

        // Ambil data dari Intent
        Intent intent = getIntent();
        idProduk = intent.getIntExtra("PRODUCT_ID", -1);
        userId = intent.getIntExtra("user_id", -1);
        usernameBuyer = intent.getStringExtra("username_buyer");

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

        btn_upload_bukti.setOnClickListener(v -> openGallery());


        btnPaid.setOnClickListener(v -> {
            if (areFieldsValid()) {
                // Tampilkan dialog
                DialogFragment dialog = new AfterPaidDialogFragment();
                dialog.show(getSupportFragmentManager(), "AfterPaidDialog");
            }
        });

        btnnotf.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {

                                           finish();
                                       }
                                   });

        btnback.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           finish();
                                   }});

        fetchAndStoreUserIdBuyer();



    }

    private boolean areFieldsValid() {
        if (imgBukti.getDrawable() == null) {
            Toast.makeText(this, "Upload Bukti Pembayaran terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (txtUsaha.getText().toString().trim().isEmpty() ||
                textNamaProduct.getText().toString().trim().isEmpty() ||
                textHarga.getText().toString().trim().isEmpty() ||
                totalharga.getText().toString().trim().isEmpty() ||
                txtRek1.getText().toString().trim().isEmpty() ||
                txtRek2.getText().toString().trim().isEmpty() ||
                txtRek3.getText().toString().trim().isEmpty() ||
                NamaCS.getText().toString().trim().isEmpty() ||
                editJumlahTiket.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private static final int PICK_IMAGE_REQUEST = 1;


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Log.d(TAG, "Image URI: " + imageUri.toString());
            imgBukti.setImageURI(imageUri);
            buktiTf = encodeImageToBase64(imageUri);
            Log.d(TAG, "Base64 String: " + buktiTf);
        }
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 5, outputStream); // Kompresi kualitas lebih rendah
            byte[] byteArray = outputStream.toByteArray();

            // Log panjang byte array untuk debug
            Log.d(TAG, "Byte array length: " + byteArray.length);

            // Encode ke Base64 menggunakan NO_WRAP
            String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            Log.d(TAG, "Base64 String length: " + base64String.length());

            return base64String;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }





    public void fetchProductDetails(int productId) {
        String url = API_PRODUK + productId;

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching user details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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

                                textNamaProduct.setText(product.getString("title"));
                                NamaCS.setText(userName);

                                String userid = product.getString("user_id");

                                int price = 0;
                                if (product.has("prices")) {
                                    JSONObject prices = product.getJSONObject("prices");

                                    int weekdayPrice = prices.optInt("weekday_price", 0);
                                    int weekendPrice = prices.optInt("weekend_price", 0);

                                    DayOfWeek dayOfWeek = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        dayOfWeek = LocalDate.now().getDayOfWeek();
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                                            price = weekendPrice;
                                        } else {
                                            price = weekdayPrice;
                                        }
                                    }
                                    textHarga.setText("Rp. " + String.format("%,d", price));
                                } else {
                                    if (product.has("weekday_price") && !product.isNull("weekday_price")) {
                                        price = product.getInt("weekday_price");
                                        textHarga.setText("Rp. " + String.format("%,d", price));
                                    } else {
                                        textHarga.setText("Price not available");
                                    }
                                }

                                updateTotalPrice(price);


                                String BASE_URL = "https://store.kemiling.com/side-page/Pendaftaran/";
                                String profileImageUrl = product.getString("img_header");
                                String fullImageUrl = BASE_URL + profileImageUrl;
                                Glide.with(imgProduct.getContext())
                                        .load(fullImageUrl)
                                        .into(imgProduct);

                                JSONArray imagesArray = product.getJSONArray("images");
                                List<String> imageUrls = new ArrayList<>();

                                for (int i = 0; i < imagesArray.length(); i++) {
                                    String imageUrl = imagesArray.getString(i);
                                    imageUrls.add(imageUrl);

                                    Log.d("Image URL", imageUrl);
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
                                fetchRekeningDetails(userid);
                                fetchAndStoreUserId(userid);
                                progressDialog.dismiss();
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

        requestQueue.add(jsonObjectRequest);
    }

    private void updateTotalPrice(int price) {
        editJumlahTiket.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int jumlahTiket = Integer.parseInt(s.toString());
                    int total = jumlahTiket * price;
                    totalharga.setText("Rp." + String.format("%,d", total));
                } catch (NumberFormatException e) {
                    totalharga.setText("Rp. 0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void fetchRekeningDetails(String userid) {
        String url = "https://store.kemiling.com/api_rekening.php?user_id=" + userid;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching rekening details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss();
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                JSONObject data = response.getJSONObject("data");

                                // Buat daftar rekening dan bank yang tersedia
                                List<Rekening> rekeningList = new ArrayList<>();
                                addRekeningIfValid(rekeningList, data.optString("bca"), "BCA");
                                addRekeningIfValid(rekeningList, data.optString("bri"), "BRI");
                                addRekeningIfValid(rekeningList, data.optString("bni"), "BNI");
                                addRekeningIfValid(rekeningList, data.optString("dana"), "DANA");
                                addRekeningIfValid(rekeningList, data.optString("gopay"), "GOPAY");
                                addRekeningIfValid(rekeningList, data.optString("shopepay"), "SHOPEPAY");
                                addRekeningIfValid(rekeningList, data.optString("qris"), "QRIS");

                                // Isi TextView berdasarkan urutan daftar rekening yang tersedia
                                if (rekeningList.size() > 0) {
                                    setRekeningAndBank(txtRek1, rek1, rekeningList.get(0));
                                }
                                if (rekeningList.size() > 1) {
                                    setRekeningAndBank(txtRek2, rek2, rekeningList.get(1));
                                }
                                if (rekeningList.size() > 2) {
                                    setRekeningAndBank(txtRek3, rek3, rekeningList.get(2));
                                }

                            } else {
                                Toast.makeText(CheckOut.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("RekeningDetail", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("RekeningDetail", "Gagal mengambil data rekening: " + error.getMessage());
                        Toast.makeText(CheckOut.this, "Gagal mengambil data rekening", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    // Metode untuk menambahkan rekening jika valid ke dalam daftar rekening
    private void addRekeningIfValid(List<Rekening> rekeningList, String nomorRekening, String namaBank) {
        if (nomorRekening != null && !nomorRekening.equals("0") && !nomorRekening.isEmpty()) {
            rekeningList.add(new Rekening(nomorRekening, namaBank));
        }
    }

    // Metode untuk mengatur nomor rekening dan nama bank ke TextView
    private void setRekeningAndBank(TextView txtRek, TextView rek, Rekening rekening) {
        txtRek.setText(rekening.nomorRekening);
        rek.setText(rekening.namaBank);
    }

    // Kelas Rekening untuk menyimpan informasi rekening
    private static class Rekening {
        String nomorRekening;
        String namaBank;

        Rekening(String nomorRekening, String namaBank) {
            this.nomorRekening = nomorRekening;
            this.namaBank = namaBank;
        }
    }


    public void fetchAndStoreUserId(String userid) {
        String url = "https://store.kemiling.com/api_endpointusername.php";

        if (userid.isEmpty()) {
            Toast.makeText(this, "Username is not available in SharedPreferences", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching user details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Tutup ProgressDialog saat respons diterima
                progressDialog.dismiss();

                Log.d(TAG, "Response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        String username = jsonResponse.getString("username"); // Mengambil username dari respons

                        // Store username in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.apply();

                        // Set teks ke txtUsaha
                        txtUsaha.setText(username);
                    } else {
                        Toast.makeText(CheckOut.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Tutup ProgressDialog saat terjadi error
                progressDialog.dismiss();

                Log.e(TAG, "Error fetching user ID", error);
                Toast.makeText(CheckOut.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userid); // Mengirimkan user_id dalam permintaan POST
                return params;
            }
        };

        queue.add(stringRequest);
    }

    String UserIdCs;

    public void fetchAndStoreUserIdBuyer() {
        String url = "https://store.kemiling.com/api_endpoint.php"; // Ganti dengan URL API yang sesuai

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        if (userName.isEmpty()) {
            Toast.makeText(this, "Username is not available in SharedPreferences", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching user details...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Tutup ProgressDialog saat respons diterima
                progressDialog.dismiss();

                Log.d(TAG, "Response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        UserIdCs = jsonResponse.getString("user_id"); // Mengambil user_id dari respons

                        // Store user_id in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_id", UserIdCs);
                        editor.apply();



                        // Hanya untuk debugging, hapus jika sudah tidak perlu
                        Log.d(TAG, "User ID: " + UserIdCs);
                    } else {
                        Toast.makeText(CheckOut.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing response", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Tutup ProgressDialog saat terjadi error
                progressDialog.dismiss();

                Log.e(TAG, "Error fetching user ID", error);
                Toast.makeText(CheckOut.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", userName); // Mengirimkan username dalam permintaan POST
                return params;
            }
        };

        queue.add(stringRequest);
    }







    public void makeOrder(int price) {
        // URL API endpoint
        String url = "https://store.kemiling.com/api_pembayaran.php/"; // Ganti dengan URL API yang sesuai

        // Ambil nilai dari input
        Intent intent = getIntent();
        int idProdukInt = intent.getIntExtra("PRODUCT_ID", -1);
        if (idProdukInt == -1) {
            Toast.makeText(this, "ID Produk tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        String idProduk = String.valueOf(idProdukInt); // Konversi ke string

        String userId = UserIdCs;
        String usernameBuyer = NamaCS.getText().toString();
        String usernameSeller = txtUsaha.getText().toString();
        String jumlah = editJumlahTiket.getText().toString();

        // Pastikan bukti_tf sudah diisi
        if (buktiTf == null) {
            Toast.makeText(this, "Bukti transfer belum dipilih", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hitung total harga
        int totalHarga = 0;
        try {
            String totalHargaStr = totalharga.getText().toString().replace("Rp.", "").replace(",", "").trim();
            if (!totalHargaStr.isEmpty()) {
                totalHarga = Integer.parseInt(totalHargaStr);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing total harga: " + e.getMessage());
            Toast.makeText(this, "Terjadi kesalahan dalam menghitung total harga", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buat JSONObject untuk data yang akan dikirim
        JSONObject postData = new JSONObject();
        try {
            postData.put("user_id", userId);
            postData.put("username_buyer", usernameBuyer);
            postData.put("username_seller", usernameSeller);
            postData.put("id_produk", idProduk);
            postData.put("jumlah", jumlah);
            postData.put("bukti_tf", buktiTf); // Base64 string
            postData.put("total_harga", totalHarga);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON data: " + e.getMessage());
            return;
        }

        // Log untuk debugging sebelum request
        Log.d(TAG, "Sending data: " + postData.toString());

// Tampilkan ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengirim data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    // Tutup ProgressDialog saat respons diterima
                    Log.d(TAG, "Response: " + response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(CheckOut.this, "Data berhasil dikirim!", Toast.LENGTH_SHORT).show();
                            Intent intent2 = new Intent(CheckOut.this, activity_beranda.class);
                            startActivity(intent2);
                        } else {
                            Toast.makeText(CheckOut.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                    }
                }, error -> {
            // Tutup ProgressDialog saat terjadi error
            progressDialog.dismiss();
            Log.e(TAG, "Error sending data", error);
            Toast.makeText(CheckOut.this, "Error sending data", Toast.LENGTH_SHORT).show();
            if (error.networkResponse != null) {
                String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                Log.e(TAG, "Error response body: " + responseBody);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);


    }




}
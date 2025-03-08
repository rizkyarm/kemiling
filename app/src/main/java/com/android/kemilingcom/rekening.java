package com.android.kemilingcom;

import static com.android.kemilingcom.Db_Contract.url_image_profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class rekening extends AppCompatActivity {

    private EditText editBCA, editBRI, editBNI, editDana, editGopay, editShopeePay;
    private TextView username;
    private Button btnSave;
    private ImageView profileImageView;
    private static final String URL_GET = "https://store.kemiling.com/api_rekening.php?id_user=7";
    private static final String URL_UPDATE = "https://store.kemiling.com/api_rekening.php";
    private static final String BASE_URL = "https://store.kemiling.com/";
    private String id_user; // Ganti dengan ID user yang sesuai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekening);

        // Inisialisasi UI
        profileImageView = findViewById(R.id.img_profile);
        username = findViewById(R.id.your_name);
        editBCA = findViewById(R.id.editbca);
        editBRI = findViewById(R.id.editbri);
        editBNI = findViewById(R.id.editbni);
        editDana = findViewById(R.id.editdana);
        editGopay = findViewById(R.id.editgopay);
        editShopeePay = findViewById(R.id.editshopepay);
        btnSave = findViewById(R.id.btn_save);



        // Ambil data rekening dari server
        getRekeningData();



        // Ambil nama pengguna dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        username.setText(userName);

        // URL API untuk gambar profil
        String apiUrl = url_image_profile + "?username=" + userName;
        // Panggil API dan ambil gambar profil
        new rekening.GetProfileImageTask().execute(apiUrl);

        // Event listener untuk tombol simpan
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateRekeningData();
            }
        });

    }

    public class GetProfileImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equals("success")) {
                    String imageUrl = jsonObject.getString("profile_image_url");
                    String fullImageUrl = BASE_URL + imageUrl;
                    // Gunakan Glide untuk memuat gambar dari URL API
                    Glide.with(rekening.this)
                            .load(fullImageUrl)
                            .into(profileImageView);
                } else {
                    // Tampilkan pesan error atau gambar default

                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }



    private void getRekeningData() {
        // Ambil user_id dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);

        if (userId == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gunakan user_id dalam URL
        String url = "https://store.kemiling.com/api_rekening.php?id_user=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            // Debug respons JSON
                            Log.d("API_RESPONSE", response);

                            // Pastikan respons memiliki objek "data"
                            if (jsonObject.has("data")) {
                                JSONObject dataObject = jsonObject.getJSONObject("data");

                                // Ambil nilai dari dalam "data"
                                editBCA.setText(dataObject.optString("bca", "0"));
                                editBRI.setText(dataObject.optString("bri", "0"));
                                editBNI.setText(dataObject.optString("bni", "0"));
                                editDana.setText(dataObject.optString("dana", "0"));
                                editGopay.setText(dataObject.optString("gopay", "0"));
                                editShopeePay.setText(dataObject.optString("shopepay", "0"));

                            } else {
                                Toast.makeText(rekening.this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(rekening.this, "Gagal memproses data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("API_ERROR", "Volley Error: " + error.getMessage());
                Toast.makeText(rekening.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void saveOrUpdateRekeningData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);

        if (userId == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://store.kemiling.com/api_rekening.php?id_user=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("API_RESPONSE", response);

                            if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
                                // Jika data sudah ada, lakukan PUT (update)
                                updateRekeningData();
                            } else {
                                // Jika data tidak ada, lakukan POST (create)
                                createRekeningData();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(rekening.this, "Gagal memproses data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("API_ERROR", "Volley Error: " + error.getMessage());
                Toast.makeText(rekening.this, "Gagal mengambil data rekening", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void createRekeningData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);
        String username = sharedPreferences.getString("username", ""); // Gantilah jika username berasal dari tempat lain

        if (userId == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://store.kemiling.com/api_rekening.php";

        HashMap<String, String> params = new HashMap<>();
        params.put("id_user", userId);
        params.put("username", username);
        params.put("bca", editBCA.getText().toString());
        params.put("bri", editBRI.getText().toString());
        params.put("bni", editBNI.getText().toString());
        params.put("dana", editDana.getText().toString());
        params.put("gopay", editGopay.getText().toString());
        params.put("shopepay", editShopeePay.getText().toString());
        params.put("qris", ""); // Sesuaikan jika ada input QRIS

        JSONObject jsonBody = new JSONObject(params);
        final String requestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("API_RESPONSE", response);
                    Toast.makeText(rekening.this, "Rekening berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("API_ERROR", "Error: " + error.getMessage());
                    Toast.makeText(rekening.this, "Gagal menambahkan rekening", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }



    private void updateRekeningData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://store.kemiling.com/api_rekening.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id_user", userId);
            jsonBody.put("bca", safeParseInt(editBCA.getText().toString()));
            jsonBody.put("bri", safeParseInt(editBRI.getText().toString()));
            jsonBody.put("bni", safeParseInt(editBNI.getText().toString()));
            jsonBody.put("dana", safeParseInt(editDana.getText().toString()));
            jsonBody.put("gopay", safeParseInt(editGopay.getText().toString()));
            jsonBody.put("shopepay", safeParseInt(editShopeePay.getText().toString()));
            jsonBody.put("qris", "");

            final String requestBody = jsonBody.toString();
            Log.d("API_REQUEST", "Request Body: " + requestBody);

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, url,
                    response -> {
                        Log.d("API_RESPONSE", "Response: " + response);
                        Toast.makeText(getApplicationContext(), "Update berhasil!", Toast.LENGTH_SHORT).show();
                    }, error -> {
                Log.e("API_ERROR", "Error: " + error.toString());
                Toast.makeText(getApplicationContext(), "Update gagal!", Toast.LENGTH_SHORT).show();
            }) {
                @Override
                public byte[] getBody() {
                    return requestBody.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan dalam memproses data", Toast.LENGTH_SHORT).show();
        }
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.isEmpty() ? "0" : value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }






}
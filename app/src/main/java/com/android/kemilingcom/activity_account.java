package com.android.kemilingcom;

import static android.content.ContentValues.TAG;
import static com.android.kemilingcom.Db_Contract.url_image_profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class activity_account extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    public TextView my_name;
    private ImageView profileImageView, logout;
    private static final String BASE_URL = "https://store.kemiling.com/";
    public Button myproduct, btn_user, btn_transaksi, btn_confirmation ,btn_rekening;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_user = findViewById(R.id.btn_user);
        my_name = findViewById(R.id.your_name);
        logout = findViewById(R.id.Logout);
        profileImageView = findViewById(R.id.img_profile);
        myproduct = findViewById(R.id.btn_myproduct);
        btn_transaksi = findViewById(R.id.btn_transaksi);
        btn_confirmation = findViewById(R.id.btn_confirmtr);
        btn_rekening = findViewById(R.id.btn_rekening);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hapus status login dari SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("userName");
                editor.remove("isLoggedIn");
                editor.apply();

                // Arahkan kembali ke activity_login
                Intent intent = new Intent(activity_account.this, activity_login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        myproduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_account.this, activityMyProduct.class);
                startActivity(intent);
            }
        });

        btn_user.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_account.this, myAccount.class);
                startActivity(intent);
            }
        });

        btn_transaksi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_account.this, transaksi.class);
                startActivity(intent);
            }
        });

        btn_confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_account.this, ConfirmationTransaction.class);
                startActivity(intent);
            }
        });

        btn_rekening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_account.this, rekening.class);
                startActivity(intent);
            }
        });

        fetchAndStoreUserIdBuyer();

        // Ambil nama pengguna dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        // Tampilkan nama pengguna
        my_name.setText(userName);

        // URL API untuk gambar profil
        String apiUrl = url_image_profile + "?username=" + userName;

        // Panggil API dan ambil gambar profil
        new GetProfileImageTask().execute(apiUrl);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Navigasi Bawah
        bottomNavigationView.setSelectedItemId(R.id.menu_akun);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        startActivity(new Intent(activity_account.this, activity_beranda.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.menu_near:
                        startActivity(new Intent(activity_account.this, activity_terdekat.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    case R.id.menu_akun:
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    private class GetProfileImageTask extends AsyncTask<String, Void, String> {
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
                    Glide.with(activity_account.this)
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
                        Toast.makeText(activity_account.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity_account.this, "Error fetching user ID", Toast.LENGTH_SHORT).show();
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
}

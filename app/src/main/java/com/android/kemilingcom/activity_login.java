package com.android.kemilingcom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class activity_login extends AppCompatActivity {

    private EditText etusername, etpassword;
    private Button btnlogin, btndaftar;
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etusername = findViewById(R.id.editusername);
        etpassword = findViewById(R.id.editpassword);
        btnlogin = findViewById(R.id.login);
        btndaftar = findViewById(R.id.daftar);

        requestPermissions();

        // Cek status login
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            Intent intent = new Intent(activity_login.this, activity_beranda.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etusername.getText().toString();
                String password = etpassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity_login.this, "Mohon isi Username dan Password", Toast.LENGTH_SHORT).show();
                } else {
                    RequestQueue requestQueue = Volley.newRequestQueue(activity_login.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, Db_Contract.url_login + "?username=" + username + "&password=" + password + "&nocache", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("Selamat Datang")) {
                                // Simpan nama pengguna ke SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userName", username);
                                editor.putBoolean("isLoggedIn", true); // Simpan status login
                                editor.apply();

                                Toast.makeText(activity_login.this, "Login Berhasil", Toast.LENGTH_SHORT).show();

                                // Meminta izin lokasi
                                requestPermissions();

                                // Arahkan ke MainActivity
                                Intent intent = new Intent(activity_login.this, activity_beranda.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(activity_login.this, "Username atau Password Salah!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(activity_login.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                    requestQueue.add(stringRequest);
                }
            }
        });

        btndaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity_login.this, activity_daftar.class);
                startActivity(intent);
            }
        });
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permissions denied
                Toast.makeText(this, "Beberapa izin ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.android.kemilingcom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class activity_daftar extends AppCompatActivity {

    private EditText etusername, etpassword;
    private Button btnlogin, btndaftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daftar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etusername = findViewById(R.id.editusernamed);
        etpassword = findViewById(R.id.editpasswordd);
        btnlogin = findViewById(R.id.login);
        btndaftar = findViewById(R.id.daftar);

        btndaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etusername.getText().toString();
                String password = etpassword.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity_daftar.this, "Mohon isi Username dan Password", Toast.LENGTH_SHORT).show();
                } else {
                    RequestQueue requestQueue = Volley.newRequestQueue(activity_daftar.this);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, Db_Contract.url_register, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                String status = jsonResponse.getString("status");
                                if (status.equals("success")) {
                                    Toast.makeText(activity_daftar.this, "Pendaftaran Berhasil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(activity_daftar.this, activity_login.class));
                                    finish();
                                } else {
                                    String error = jsonResponse.getString("error");
                                    Toast.makeText(activity_daftar.this, "Pendaftaran Gagal: " + error, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(activity_daftar.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(activity_daftar.this, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        public byte[] getBody() {
                            Map<String, String> params = new HashMap<>();
                            params.put("username", username);
                            params.put("password", password);
                            return new JSONObject(params).toString().getBytes();
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }
                    };
                    requestQueue.add(stringRequest);
                }
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity_daftar.this, activity_login.class));
            }
        });
    }
}

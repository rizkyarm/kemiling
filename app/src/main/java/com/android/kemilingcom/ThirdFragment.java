package com.android.kemilingcom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ThirdFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private Button btn_confirmer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        recyclerView = view.findViewById(R.id.recycleview3);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList, getContext(), true); // Pass true to show buttons


        recyclerView.setAdapter(transactionAdapter);

        // Retrieve user_id from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");

        // Print debug statement
        Log.d("Debug", "Retrieved user ID: " + userName);

        if (!userName.isEmpty()) {
            getTransactions((userName));
        } else {
            Toast.makeText(getContext(), "Username not available", Toast.LENGTH_SHORT).show();
        }

        // Implement the back button callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle back press
                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return view;


    }

    public void getTransactions(String userName) {


        String url = "https://store.kemiling.com/api_transaksi_seller.php?username_seller=" + userName;

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(String response) {
                        // Handle the response
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int validasiPembayaran = jsonObject.getInt("validasi_pembayaran");

                                if (validasiPembayaran == 0) {
                                    Transaction transaction = new Transaction();
                                    transaction.setId_transaksi(jsonObject.getInt("id_transaksi"));
                                    transaction.setUser_id(jsonObject.getInt("user_id"));
                                    transaction.setUsername_buyer(jsonObject.getString("username_buyer"));
                                    transaction.setId_produk(jsonObject.getInt("id_produk"));
                                    transaction.setUsername_seller(jsonObject.getString("username_seller"));
                                    transaction.setNama_produk(jsonObject.getString("nama_produk"));
                                    transaction.setWeekday_ticket(jsonObject.getInt("weekday_ticket"));
                                    transaction.setWeekend_ticket(jsonObject.getInt("weekend_ticket"));
                                    transaction.setHarga_produk(jsonObject.getInt("harga_produk"));
                                    transaction.setJumlah(jsonObject.getInt("jumlah"));
                                    transaction.setTotal_harga(jsonObject.getInt("total_harga"));
                                    transaction.setAlamat(jsonObject.getString("alamat"));
                                    transaction.setBukti_tf(jsonObject.getString("bukti_tf"));
                                    transaction.setValidasi_pembayaran(validasiPembayaran);
                                    transaction.setDatetime(jsonObject.getString("datetime"));
                                    transactionList.add(transaction);
                                }
                            }
                            transactionAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                error.printStackTrace();
            }
        });

        requestQueue.add(stringRequest);
    }
}

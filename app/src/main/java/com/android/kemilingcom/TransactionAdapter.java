package com.android.kemilingcom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactionList;
    private Context context;
    private boolean showButtons;

    public TransactionAdapter(List<Transaction> transactionList, Context context, boolean b) {
        this.transactionList = transactionList;
        this.context = context;
        this.showButtons = b;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.txtToko.setText(transaction.getUsername_seller());
        holder.txtNamaProduk.setText(transaction.getNama_produk());
        holder.txtJumlahTiket.setText(String.valueOf(transaction.getJumlah()));
        holder.txtTotalHarga.setText(String.valueOf(transaction.getTotal_harga()));
        holder.txtKonfirmasi.setText(transaction.getValidasi_pembayaran() == 1 ? "Confirmed" : "Not Confirmed");
        holder.txtDate.setText(transaction.getDatetime());

        String img_url = "https://store.kemiling.com/";
        String full_image_url = img_url + transaction.getBukti_tf();

        // Load image using Glide
        Glide.with(context)
                .load(full_image_url) // Full URL of the image
                .override(200, 250)
                .centerCrop()
                .into(holder.imgProduk);

        // Set click listener for the image
        holder.imgProduk.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putExtra("image_url", full_image_url);
            context.startActivity(intent);
        });

        // Show or hide buttons based on showButtons flag
        if (showButtons) {
            holder.btnKonfirmasi.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnKonfirmasi.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        }

        // Set click listener for btnKonfirmasi
        holder.btnKonfirmasi.setOnClickListener(v -> {
            updateValidation(transaction.getId_transaksi(), holder, 1);
        });

        // Set click listener for btnCancel
        holder.btnCancel.setOnClickListener(v -> {
            updateValidation(transaction.getId_transaksi(), holder, 0);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtToko, txtNamaProduk, txtJumlahTiket, txtTotalHarga, txtKonfirmasi, txtDate;
        public ImageView imgProduk;
        public Button btnKonfirmasi, btnCancel;

        public ViewHolder(View itemView) {
            super(itemView);
            txtToko = itemView.findViewById(R.id.txt_toko);
            txtNamaProduk = itemView.findViewById(R.id.txt_nama_produk);
            txtJumlahTiket = itemView.findViewById(R.id.txt_jumlah_tiket);
            txtTotalHarga = itemView.findViewById(R.id.txt_totalharga);
            txtKonfirmasi = itemView.findViewById(R.id.txt_konfirmasi);
            txtDate = itemView.findViewById(R.id.txt_date);
            imgProduk = itemView.findViewById(R.id.img_produk);
            btnKonfirmasi = itemView.findViewById(R.id.btn_konfirmasi);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }

    // Fungsi updateValidation dengan tambahan parameter untuk validasi_pembayaran
    private void updateValidation(int id_transaksi, ViewHolder holder, int validasi_pembayaran) {
        String url = "https://store.kemiling.com/api_transaksi_seller.php";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response
                        Log.d("Response", response);
                        holder.txtKonfirmasi.setText(validasi_pembayaran == 1 ? "Confirmed" : "Not Confirmed");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e("Error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_transaksi", String.valueOf(id_transaksi));
                params.put("validasi_pembayaran", String.valueOf(validasi_pembayaran));
                Log.d("Params", "id_transaksi: " + id_transaksi + ", validasi_pembayaran: " + validasi_pembayaran); // Tambahkan log untuk parameter
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}

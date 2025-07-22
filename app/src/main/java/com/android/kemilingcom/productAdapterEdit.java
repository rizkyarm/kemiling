package com.android.kemilingcom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Calendar;

public class productAdapterEdit extends RecyclerView.Adapter<productAdapterEdit.DefaultViewHolder> {
    private Context context;
    private List<product> productList;
    private boolean isActivityMyProduct;

    public productAdapterEdit(Context context, List<product> productList) {
        this.context = context;
        this.productList = productList;
        this.isActivityMyProduct = false;
    }

    public productAdapterEdit(Context context, List<product> productList, boolean isActivityMyProduct) {
        this.context = context;
        this.productList = productList;
        this.isActivityMyProduct = isActivityMyProduct;
    }

    @NonNull
    @Override
    public DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_edit, parent, false);
        return new DefaultViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        product product = productList.get(position);

        holder.tvTitle.setText(product.getTitle());

        String category = product.getCategory();
        Log.d("ADAPTER_DEBUG", "Category: " + category);
        Log.d("ADAPTER_DEBUG", "Weekday Ticket: " + product.getWeekdayTicket());
        Log.d("ADAPTER_DEBUG", "Weekend Ticket: " + product.getWeekendTicket());

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        if (category != null && category.toLowerCase().contains("wisata")) {
            int harga = isWeekend ? product.getWeekendTicket() : product.getWeekdayTicket();
            String hargaTiket = "Rp. " + String.format("%,d", harga);
            holder.tvPrice.setText(hargaTiket);
        } else {
            holder.tvPrice.setText("Rp. " + String.format("%,d", product.getPrice()));
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.raw.cicle_animation)
                    .error(R.raw.cicle_animation)
                    .override(1100, 750)
                    .centerCrop()
                    .into(holder.ivPicture);
        } else {
            Glide.with(context)
                    .load(R.raw.cicle_animation)
                    .into(holder.ivPicture);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, activity_product.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            intent.putExtra("PRODUCT_TITLE", product.getTitle());
            intent.putExtra("PRODUCT_PRICE", product.getPrice());
            intent.putExtra("PRODUCT_LOCATION", product.getLocation());
            intent.putExtra("PRODUCT_RATING", product.getRating());
            intent.putExtra("PRODUCT_IMAGE", product.getImageUrl());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(product.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class DefaultViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPicture;
        TextView tvTitle;
        TextView tvPrice;
        ImageView btnDelete;

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.picture);
            tvTitle = itemView.findViewById(R.id.title);
            tvPrice = itemView.findViewById(R.id.price);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void showDeleteConfirmationDialog(int productId, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapusnya?")
                .setPositiveButton("Ya", (dialog, which) -> deleteProduct(productId, position))
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteProduct(int productId, int position) {
        String url = "https://store.kemiling.com/api_deleteproduct.php?id=" + productId;

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = jsonObject.getBoolean("status");
                        String message = jsonObject.getString("message");

                        if (status) {
                            productList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, productList.size());
                            Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Kesalahan parsing respon", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

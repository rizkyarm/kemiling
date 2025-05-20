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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class productAdapter extends RecyclerView.Adapter<productAdapter.DefaultViewHolder> {
    private Context context;
    private List<product> productList;
    private boolean isActivityMyProduct;

    // Overloaded constructor for backward compatibility
    public productAdapter(Context context, List<product> productList) {
        this.context = context;
        this.productList = productList;
        this.isActivityMyProduct = false; // Default to false for backward compatibility
    }

    public productAdapter(Context context, List<product> productList, boolean isActivityMyProduct) {
        this.context = context;
        this.productList = productList;
        this.isActivityMyProduct = isActivityMyProduct;
    }

    @NonNull
    @Override
    public DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_item, parent, false);
        return new DefaultViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        product product = productList.get(position);

        // Set judul produk
        String fullTitle = product.getTitle();
        String shortTitle = fullTitle.length() > 13 ? fullTitle.substring(0, 12) + "..." : fullTitle;
        holder.tvTitle.setText(shortTitle);

        // Tentukan harga berdasarkan kategori
        if (product.getCategory().equalsIgnoreCase("UMKM")) {
            holder.tvPrice.setText("Rp. " + String.format("%,d", product.getPrice()));
        } else if (product.getCategory().equalsIgnoreCase("Tempat Wisata")) {
            // Tentukan harga berdasarkan hari saat ini
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int displayedPrice = (currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY) ?
                    product.getWeekendTicket() : product.getWeekdayTicket();

            if (product.getCategory().equalsIgnoreCase("UMKM")) {
                holder.tvPrice.setText("Rp. " + String.format("%,d", product.getPrice()));
            }
            else if (product.getCategory().equalsIgnoreCase("Tempat Wisata")) {
                currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                displayedPrice = (currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY) ?
                        product.getWeekendTicket() : product.getWeekdayTicket();

                if (displayedPrice > 0) {
                    holder.tvPrice.setText("Rp. " + String.format("%,d", displayedPrice));
                } else {
                    holder.tvPrice.setText("Harga Tidak Tersedia");
                }
            }        }

        // Cek apakah URL gambar tersedia
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Log.d("Glide", "Loading image from: " + product.getImageUrl());

            Glide.with(holder.ivPicture.getContext())  // ✅ Gunakan context dari View
                    .load(product.getImageUrl())
                    .placeholder(R.raw.cicle_animation)
                    .error(R.raw.cicle_animation)
                    .override(1100, 750)
                    .centerCrop()
                    .into(holder.ivPicture);
        } else {
            Log.e("Glide", "Image URL is empty, loading default image.");
            Glide.with(holder.ivPicture.getContext())  // ✅ Ganti di sini juga
                    .load(R.raw.cicle_animation)
                    .into(holder.ivPicture);
        }


        // Handle klik item untuk membuka activity_product
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, activity_product.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            intent.putExtra("PRODUCT_TITLE", product.getTitle());
            intent.putExtra("PRODUCT_PRICE", product.getPrice());
            intent.putExtra("PRODUCT_WEEKDAY_TICKET", product.getWeekdayTicket());
            intent.putExtra("PRODUCT_WEEKEND_TICKET", product.getWeekendTicket());
            intent.putExtra("PRODUCT_CATEGORY", product.getCategory());
            intent.putExtra("PRODUCT_LOCATION", product.getLocation());
            intent.putExtra("PRODUCT_RATING", product.getRating());
            intent.putExtra("PRODUCT_IMAGE", product.getImageUrl());
            context.startActivity(intent);
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

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.picture);
            tvTitle = itemView.findViewById(R.id.title);
            tvPrice = itemView.findViewById(R.id.price);
        }
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

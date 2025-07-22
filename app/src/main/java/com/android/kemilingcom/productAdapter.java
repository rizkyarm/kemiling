package com.android.kemilingcom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class productAdapter extends RecyclerView.Adapter<productAdapter.DefaultViewHolder> {
    private Context context;
    private List<product> productList;
    private boolean isActivityMyProduct;

    public productAdapter(Context context, List<product> productList) {
        this.context = context;
        this.productList = productList;
        this.isActivityMyProduct = false;
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

    @SuppressLint({"ResourceType", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        product product = productList.get(position);

        String fullTitle = product.getTitle();
        String shortTitle = fullTitle.length() > 13 ? fullTitle.substring(0, 12) + "..." : fullTitle;
        holder.tvTitle.setText(shortTitle);

        // REVISI 2: Menambahkan logika untuk menampilkan jarak
        double distance = product.getDistanceRouteKm();
        if (distance > 0) {
            holder.tvJarak.setVisibility(View.VISIBLE);
            holder.tvJarak.setText(String.format(Locale.US, "%.1f KM", distance));
        } else {
            // Sembunyikan TextView jika tidak ada jarak (misal: di halaman Beranda)
            holder.tvJarak.setVisibility(View.GONE);
        }

        if (product.getCategory().equalsIgnoreCase("UMKM")) {
            holder.tvPrice.setText("Rp. " + String.format("%,d", product.getPrice()));
        } else if (product.getCategory().equalsIgnoreCase("Tempat Wisata")) {
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int displayedPrice = (currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY) ?
                    product.getWeekendTicket() : product.getWeekdayTicket();
            if (displayedPrice > 0) {
                holder.tvPrice.setText("Rp. " + String.format("%,d", displayedPrice));
            } else {
                holder.tvPrice.setText("Harga Bervariasi");
            }
        }

        loadImageSafely(holder, product);

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
            // REVISI 3: Menambahkan data jarak ke intent
            intent.putExtra("PRODUCT_DISTANCE", product.getDistanceRouteKm());
            context.startActivity(intent);
        });
    }

    @SuppressLint("ResourceType")
    private void loadImageSafely(DefaultViewHolder holder, product product) {
        // ... (Fungsi ini tidak perlu diubah)
        try {
            Context safeContext = holder.ivPicture.getContext();
            if (!isContextValid()) {
                Log.w("Glide", "Context not valid, skip loading image");
                return;
            }

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(safeContext)
                        .load(product.getImageUrl())
                        .placeholder(R.raw.cicle_animation)
                        .error(R.raw.cicle_animation)
                        .override(1100, 750)
                        .centerCrop()
                        .into(holder.ivPicture);
            } else {
                Glide.with(safeContext)
                        .load(R.raw.cicle_animation)
                        .into(holder.ivPicture);
            }
        } catch (Exception e) {
            Log.e("Glide", "Error loading image: " + e.getMessage(), e);
            holder.ivPicture.setImageResource(R.raw.cicle_animation);
        }
    }

    private boolean isContextValid() {
        // ... (Fungsi ini tidak perlu diubah)
        if (context == null) return false;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void cleanup() {
        // ... (Fungsi ini tidak perlu diubah)
        if (context != null) {
            try {
                Glide.with(context).clear((Target<?>) context);
            } catch (Exception e) {
                Log.e("Glide", "Error during cleanup: " + e.getMessage());
            }
        }
    }

    public static class DefaultViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPicture;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvJarak; // REVISI 1: Menambahkan referensi TextView untuk jarak

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.picture);
            tvTitle = itemView.findViewById(R.id.title);
            tvPrice = itemView.findViewById(R.id.price);
            tvJarak = itemView.findViewById(R.id.jarak_txt); // Menghubungkan ke ID di XML
        }
    }
}
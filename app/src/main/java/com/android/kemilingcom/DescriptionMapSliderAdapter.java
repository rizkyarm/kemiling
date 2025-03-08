package com.android.kemilingcom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.MapView;
import java.util.ArrayList;
import java.util.List;

public class DescriptionMapSliderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DESCRIPTION = 0;
    private static final int VIEW_TYPE_MAP = 1;

    private List<Object> pages;

    public DescriptionMapSliderAdapter() {
        pages = new ArrayList<>();
    }

    public void addPage(Object page) {
        pages.add(page);
        notifyDataSetChanged();
    }

    public void updateDescription(String description) {
        if (!pages.isEmpty() && pages.get(0) instanceof String) {
            pages.set(0, description);
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object page = pages.get(position);
        if (page instanceof String) {
            return VIEW_TYPE_DESCRIPTION;
        } else if (page instanceof MapView) {
            return VIEW_TYPE_MAP;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DESCRIPTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.description_item, parent, false);
            return new DescriptionViewHolder(view);
        } else if (viewType == VIEW_TYPE_MAP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_item, parent, false);
            return new MapViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object page = pages.get(position);
        if (holder instanceof DescriptionViewHolder) {
            ((DescriptionViewHolder) holder).bind((String) page);
        } else if (holder instanceof MapViewHolder) {
            ((MapViewHolder) holder).bind((MapView) page);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class DescriptionViewHolder extends RecyclerView.ViewHolder {
        private TextView descriptionTextView;

        DescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.description_text);
        }

        void bind(String description) {
            descriptionTextView.setText(description);
        }
    }

    static class MapViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup mapContainer;

        MapViewHolder(@NonNull View itemView) {
            super(itemView);
            mapContainer = itemView.findViewById(R.id.map_container);
        }

        void bind(MapView mapView) {
            if (mapView.getParent() != null) {
                ((ViewGroup) mapView.getParent()).removeView(mapView);
            }
            mapContainer.addView(mapView);
        }
    }
}

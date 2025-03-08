package com.android.kemilingcom;

import static com.android.kemilingcom.R.id.full_screen_image;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView fullScreenImageView = findViewById(full_screen_image);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("image_url")) {
            String imageUrl = intent.getStringExtra("image_url");

            // Load the image using Glide
            Glide.with(this)
                    .load(imageUrl)
                    .into(fullScreenImageView);
        }

        // Close the activity when the image is clicked
        fullScreenImageView.setOnClickListener(v -> finish());
    }
}

package com.android.kemilingcom;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class transaksi extends AppCompatActivity {

    private TextView btnNotConfirm;
    private TextView btnConfirm;
    private ImageView btnBack;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaksi);

        // Set padding to avoid system bars overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnNotConfirm = findViewById(R.id.btn_not_confirm);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnBack = findViewById(R.id.btn_back6);


        // Default fragment
        if (savedInstanceState == null) {
            switchToFragment(new FirstFragment());
            highlightButton(btnNotConfirm);
        }

        btnNotConfirm.setOnClickListener(v -> {
            switchToFragment(new FirstFragment());
            highlightButton(btnNotConfirm);
        });

        btnConfirm.setOnClickListener(v -> {
            switchToFragment(new SecondFragment());
            highlightButton(btnConfirm);
        });

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void switchToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void highlightButton(TextView buttonToHighlight) {
        btnNotConfirm.setTextColor(Color.BLACK);
        btnConfirm.setTextColor(Color.BLACK);
        buttonToHighlight.setTextColor(new Color().parseColor("#589058"));
    }



}

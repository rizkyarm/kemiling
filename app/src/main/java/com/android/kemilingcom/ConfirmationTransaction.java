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

public class ConfirmationTransaction extends AppCompatActivity {

    private TextView btnNotConfirm;
    private TextView btnConfirm;
    private ImageView btnBack;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmation_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnNotConfirm = findViewById(R.id.btn_not_confirm2);
        btnConfirm = findViewById(R.id.btn_confirm2);
        btnBack = findViewById(R.id.btn_back7);


        // Default fragment
        if (savedInstanceState == null) {
            switchToFragment(new ThirdFragment());
            highlightButton(btnNotConfirm);
        }

        btnNotConfirm.setOnClickListener(v -> {
            switchToFragment(new ThirdFragment());
            highlightButton(btnNotConfirm);
        });

        btnConfirm.setOnClickListener(v -> {
            switchToFragment(new FourthFragment());
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
        transaction.replace(R.id.fragmen_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void highlightButton(TextView buttonToHighlight) {
        btnNotConfirm.setTextColor(Color.BLACK);
        btnConfirm.setTextColor(Color.BLACK);
        buttonToHighlight.setTextColor(new Color().parseColor("#589058"));
    }
}
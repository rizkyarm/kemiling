package com.android.kemilingcom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

public class AfterPaidDialogFragment extends DialogFragment {

    private float initialY;
    private float lastY;
    private int price;

    public AfterPaidDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_after_paid, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView blockBlack = view.findViewById(R.id.block_black);
        blockBlack.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialY = event.getRawY();
                    lastY = initialY;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float currentY = event.getRawY();
                    float deltaY = currentY - lastY;
                    blockBlack.setTranslationY(blockBlack.getTranslationY() + deltaY);
                    lastY = currentY;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (blockBlack.getTranslationY() > blockBlack.getHeight()) {
                        dismiss();
                    } else {
                        blockBlack.setTranslationY(0);
                    }
                    return true;
            }
            return false;
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set touch listener on the root view to dismiss the dialog when clicking outside the content
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(view.findViewById(R.id.main), event)) {
                dismiss();
                return true;
            }
            return false;
        });

        // Set listener on btn_done
        Button btnDone = view.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(v -> {
            CheckOut activity = (CheckOut) getActivity();
            if (activity != null) {
                activity.makeOrder(price); // Panggil metode makeOrder() di aktivitas CheckOut
                dismiss();

            }
        });
    }

    private boolean isOutOfBounds(View v, MotionEvent event) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return event.getRawX() < x || event.getRawX() > x + v.getWidth() || event.getRawY() < y || event.getRawY() > y + v.getHeight();
    }
}

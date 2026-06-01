package com.teknisio.mobile.view.onboarding;

import com.teknisio.mobile.base.BaseActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatButton;

import com.teknisio.mobile.MainActivity;
import com.teknisio.mobile.R;

public class OnboardingActivity extends BaseActivity {

    private ImageView imgOnboarding;
    private View dot1;
    private View dot2;
    private View dot3;
    private AppCompatButton btnStart;

    private int currentIndex = 0;

    private final int[] images = {
            R.drawable.onboarding_1,
            R.drawable.onboarding_2,
            R.drawable.onboarding_3
    };

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable autoSlideRunnable = new Runnable() {
        @Override
        public void run() {
            int nextIndex = currentIndex + 1;

            if (nextIndex >= images.length) {
                nextIndex = 0;
            }

            setSlide(nextIndex, true);
            handler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_onboarding);

        imgOnboarding = findViewById(R.id.imgOnboarding);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnStart = findViewById(R.id.btnStart);

        setSlide(0, false);

        btnStart.setOnClickListener(v -> goToRouter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(autoSlideRunnable);
        handler.postDelayed(autoSlideRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoSlideRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoSlideRunnable);
    }

    private void makeFullScreen() {
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    private void setSlide(int index, boolean animate) {
        currentIndex = index;

        if (animate) {
            imgOnboarding.animate()
                    .alpha(0f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        imgOnboarding.setImageResource(images[currentIndex]);
                        imgOnboarding.animate()
                                .alpha(1f)
                                .setDuration(160)
                                .start();
                    })
                    .start();
        } else {
            imgOnboarding.setImageResource(images[currentIndex]);
            imgOnboarding.setAlpha(1f);
        }

        updateDots();
    }

    private void updateDots() {
        dot1.setBackgroundResource(currentIndex == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        dot2.setBackgroundResource(currentIndex == 1 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        dot3.setBackgroundResource(currentIndex == 2 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
    }

    private void goToRouter() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

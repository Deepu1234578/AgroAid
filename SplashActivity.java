package com.example.agroaid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    ImageView logo;
    TextView appName;
    LinearLayout titleGroup;

    String text = "AgroAid";
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.appName);
        titleGroup = findViewById(R.id.titleGroup);

        appName.setText(""); // IMPORTANT: clear text before animation

        // 1️⃣ Logo animation
        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .withEndAction(this::startReveal)
                .start();

        // ✅ SAFETY: Force navigation after 5 sec (backup)

    }

    private void startReveal() {

        // 2️⃣ Move logo + text
        titleGroup.post(() -> {

            float screenCenter = titleGroup.getRootView().getWidth() / 2f;
            float viewCenter = titleGroup.getX() + titleGroup.getWidth() / 2f;

            float moveDistance = screenCenter - viewCenter;

            titleGroup.animate()
                    .translationX(moveDistance)
                    .setDuration(2000)
                    .start();
        });

        // 3️⃣ Text typing animation
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (index < text.length()) {
                    appName.append(String.valueOf(text.charAt(index)));
                    index++;
                    handler.postDelayed(this, 150);
                } else {

                    // 4️⃣ Go to LoginActivity (NOT MainActivity)
                    new Handler().postDelayed(() -> {

                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        }

                        finish();

                    }, 1000);
                }
            }
        };

        handler.postDelayed(runnable, 200);
    }
}
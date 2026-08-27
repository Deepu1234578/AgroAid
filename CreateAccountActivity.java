package com.example.agroaid;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {

    EditText email, password, confirmPassword;
    Button createBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        // FORM
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        createBtn = findViewById(R.id.createBtn);

        // ANIMATION VIEWS
        ImageView bgImage = findViewById(R.id.bgImage);
        LinearLayout logoSection = findViewById(R.id.logoSection);
        LinearLayout createCard = findViewById(R.id.createCard);
        TextView tvBottomLogin = findViewById(R.id.tvBottomLogin);

        // BACKGROUND ANIMATION
        Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_slow);
        bgImage.startAnimation(zoomAnim);

        // INITIAL STATES
        logoSection.setAlpha(0f);
        logoSection.setTranslationY(50f);

        createCard.setAlpha(0f);
        createCard.setTranslationY(70f);

        tvBottomLogin.setAlpha(0f);

        // LOGO ANIMATION
        logoSection.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(900)
                .setStartDelay(200)
                .start();

        // CARD ANIMATION
        createCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(900)
                .setStartDelay(500)
                .start();

        // LOGIN TEXT ANIMATION
        tvBottomLogin.animate()
                .alpha(1f)
                .setDuration(700)
                .setStartDelay(900)
                .start();

        // CREATE ACCOUNT BUTTON
        createBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirm = confirmPassword.getText().toString().trim();

            if (userEmail.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                confirmPassword.setError("Passwords do not match");
                return;
            }

            mAuth.createUserWithEmailAndPassword(userEmail, pass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(this,
                                    "Account Created",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent =
                                    new Intent(this, EmailLoginActivity.class);

                            intent.putExtra("email", userEmail);
                            intent.putExtra("password", pass);

                            startActivity(intent);

                            overridePendingTransition(
                                    R.anim.slide_left_fast,
                                    R.anim.zoom_out
                            );

                            finish();

                        } else {

                            Toast.makeText(
                                    this,
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}
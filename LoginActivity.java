package com.example.agroaid;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;

    // Views
    ImageView bgImage;
    LinearLayout logoSection, glassCard;
    TextView tvTagline, tvBottomTagline;
    Button googleBtn, createAccountBtn, btnEmailSignIn;
    TextView tvAlreadyMember;
    LinearLayout emailLoginSection;
    EditText etEmail, etPassword;

    boolean isEmailSectionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        // Bind views
        bgImage         = findViewById(R.id.bgImage);
        logoSection     = findViewById(R.id.logoSection);
        glassCard       = findViewById(R.id.glassCard);
        tvTagline       = findViewById(R.id.tvTagline);
        googleBtn       = findViewById(R.id.googleBtn);
        createAccountBtn= findViewById(R.id.createAccountBtn);
        btnEmailSignIn  = findViewById(R.id.btnEmailSignIn);
        tvAlreadyMember = findViewById(R.id.tvAlreadyMember);
        emailLoginSection = findViewById(R.id.emailLoginSection);
        etEmail         = findViewById(R.id.etEmail);
        etPassword      = findViewById(R.id.etPassword);

        // Hint color
        int hintColor = Color.parseColor("#5A7A5A");
        etEmail.setHintTextColor(hintColor);
        etPassword.setHintTextColor(hintColor);

        // Google config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("789152604927-arpo2clgcdb4372l78v0p2130n0e78m0.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ── START ANIMATIONS ──
        startEntranceAnimations();

        // Listeners
        createAccountBtn.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccountActivity.class)));

        googleBtn.setOnClickListener(v ->
                startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN));

        tvAlreadyMember.setOnClickListener(v -> toggleEmailSection());

        btnEmailSignIn.setOnClickListener(v -> signInWithEmail());
    }

    /**
     * Orchestrates the full entrance animation sequence:
     * 1. Background: slow zoom out-to-in (6 seconds, Ken Burns style)
     * 2. Logo: fades + slides up after 200ms
     * 3. Glass card: fades + slides up after 600ms
     * 4. Bottom tagline: fades in after 1000ms
     */
    private void startEntranceAnimations() {
        Handler handler = new Handler(Looper.getMainLooper());

        // ── 1. Background: slow zoom (Ken Burns) ──
        if (bgImage != null) {
            Animation zoomAnim = AnimationUtils.loadAnimation(this, R.anim.zoom_out_slow);
            bgImage.startAnimation(zoomAnim);
        }

        // ── 2. Hide all UI elements initially ──
        if (logoSection != null)     { logoSection.setAlpha(0f);     logoSection.setTranslationY(50f); }
        if (glassCard != null)       { glassCard.setAlpha(0f);       glassCard.setTranslationY(60f); }
        if (tvBottomTagline != null) { tvBottomTagline.setAlpha(0f); }

        // ── 3. Logo animates in at 200ms ──
        handler.postDelayed(() -> {
            if (logoSection != null) {
                AnimatorSet logoAnim = new AnimatorSet();
                logoAnim.playTogether(
                        ObjectAnimator.ofFloat(logoSection, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(logoSection, "translationY", 50f, 0f)
                );
                logoAnim.setDuration(800);
                logoAnim.setInterpolator(new DecelerateInterpolator(1.5f));
                logoAnim.start();
            }
        }, 200);

        // ── 4. Glass card animates in at 600ms ──
        handler.postDelayed(() -> {
            if (glassCard != null) {
                AnimatorSet cardAnim = new AnimatorSet();
                cardAnim.playTogether(
                        ObjectAnimator.ofFloat(glassCard, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(glassCard, "translationY", 60f, 0f)
                );
                cardAnim.setDuration(800);
                cardAnim.setInterpolator(new DecelerateInterpolator(1.5f));
                cardAnim.start();
            }
        }, 600);

        // ── 5. Bottom tagline fades in at 1000ms ──
        handler.postDelayed(() -> {
            if (tvBottomTagline != null) {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvBottomTagline, "alpha", 0f, 1f);
                fadeIn.setDuration(600);
                fadeIn.start();
            }
        }, 1000);
    }

    private void toggleEmailSection() {
        if (!isEmailSectionVisible) {
            emailLoginSection.setVisibility(View.VISIBLE);
            emailLoginSection.setAlpha(0f);
            emailLoginSection.setTranslationY(-30f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(emailLoginSection, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(emailLoginSection, "translationY", -30f, 0f)
            );
            set.setDuration(350);
            set.setInterpolator(new DecelerateInterpolator());
            set.start();

            tvAlreadyMember.setText("HIDE LOGIN ↑");
            isEmailSectionVisible = true;
        } else {
            ObjectAnimator alpha = ObjectAnimator.ofFloat(emailLoginSection, "alpha", 1f, 0f);
            alpha.setDuration(250);
            alpha.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    emailLoginSection.setVisibility(View.GONE);
                }
            });
            alpha.start();
            tvAlreadyMember.setText("LOGIN");
            isEmailSectionVisible = false;
        }
    }

    private void signInWithEmail() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty())    { etEmail.setError("Enter your email");       etEmail.requestFocus();    return; }
        if (password.isEmpty()) { etPassword.setError("Enter your password"); etPassword.requestFocus(); return; }

        btnEmailSignIn.setEnabled(false);
        btnEmailSignIn.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnEmailSignIn.setEnabled(true);
                    btnEmailSignIn.setText("SIGN IN");
                    if (task.isSuccessful()) {
                        sendLoginNotification("Welcome back! 🌿", "You're signed in to AgroAid.");
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage() : "Sign in failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN) return;
        try {
            GoogleSignInAccount account = GoogleSignIn
                    .getSignedInAccountFromIntent(data).getResult(ApiException.class);
            mAuth.signInWithCredential(
                            GoogleAuthProvider.getCredential(account.getIdToken(), null))
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String name = (user != null && user.getDisplayName() != null)
                                    ? user.getDisplayName() : "Farmer";
                            sendLoginNotification("Welcome, " + name + "! 🌿",
                                    "Signed in via Google. Happy farming!");
                        } else {
                            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (ApiException e) {
            Toast.makeText(this, "Google Error: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendLoginNotification(String title, String body) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic("agroaid_users")
                .addOnCompleteListener(task -> {
                    Notificationhelper.showNotification(LoginActivity.this, title, body);
                    goToMain();
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
package com.example.agroaid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class EmailLoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

        // Receive data from Create Account
        String emailData = getIntent().getStringExtra("email");
        String passData = getIntent().getStringExtra("password");

        if (emailData != null) email.setText(emailData);
        if (passData != null) password.setText(passData);

        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (userEmail.isEmpty()) {
                email.setError("Enter email");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                email.setError("Invalid email");
                return;
            }

            if (userPass.isEmpty()) {
                password.setError("Enter password");
                return;
            }

            mAuth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, MainActivity.class));
                            finish();

                        } else {

                            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
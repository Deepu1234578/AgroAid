
        package com.example.agroaid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    EditText name,email,password;
    Button createBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singnup);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createBtn = findViewById(R.id.createBtn);

        mAuth = FirebaseAuth.getInstance();

        createBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if(userEmail.isEmpty() || userPassword.isEmpty()){
                Toast.makeText(this,"Enter Email & Password",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(userEmail,userPassword)
                    .addOnCompleteListener(task -> {

                        if(task.isSuccessful()){

                            Toast.makeText(this,"Account Created",Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this,LoginActivity.class));
                            finish();

                        }else{

                            Toast.makeText(this,"Signup Failed",Toast.LENGTH_SHORT).show();
                        }

                    });

        });

    }
}


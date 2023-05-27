package com.devarshukani.green_user.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView loginLink;
    private EditText registerEmail;
    private EditText registerPassword,registerRePassword;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginLink = findViewById(R.id.loginLink);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerRePassword = findViewById(R.id.registerRePassword);
        registerButton = findViewById(R.id.registerButton);

        auth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString().trim();
                String password  = registerPassword.getText().toString().trim();
                String repassword  = registerRePassword.getText().toString().trim();

                if(email.isEmpty()){
                    registerEmail.setError("Email cannot be empty");
                }
                else if(password.isEmpty()){
                    registerPassword.setError("Password cannot be empty");
                }
                else if (repassword.isEmpty()) {
                    registerRePassword.setError("Confirm Password cannot be empty");
                }
                else if (!repassword.equals(password)) {
                    registerRePassword.setError("Values does not match");
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), RegisterUserDetailsActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                }


            }
        });


        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                RegisterActivity.this.finish();
            }
        });
    }

}
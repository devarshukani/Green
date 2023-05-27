package com.devarshukani.green_user.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView registerLink;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton;
    private FirebaseFirestore db;
    String usertype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerLink = findViewById(R.id.registerLink);
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        String userId = preferences.getString("userId", null);

        if (userId != null) {
//            db.collection("users").document(auth.getCurrentUser().getUid())
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if (task.isSuccessful()) {
//                                DocumentSnapshot document = task.getResult();
//
//                                if (document.exists()) {
//                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                    startActivity(intent);
//                                    LoginActivity.this.finish();
//                                } else {
//                                    Intent intent = new Intent(LoginActivity.this, RegisterUserDetailsActivity.class);
//                                    startActivity(intent);
//                                    LoginActivity.this.finish();
//                                }
//                            } else {
//                                Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = loginEmail.getText().toString().trim();
                String password  = loginPassword.getText().toString().trim();

                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    if(!password.isEmpty()){
                        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                String uid = authResult.getUser().getUid();

                                db.collection("users").document(uid)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    String usertype = documentSnapshot.getString("usertype");

                                                    if(usertype.equals("regular")){
                                                        String userId = auth.getCurrentUser().getUid();
                                                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putString("userId", userId);
                                                        editor.apply();

                                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                        LoginActivity.this.finish();
                                                    }
                                                    else{
                                                        Toast.makeText(LoginActivity.this, "User doesn't have this auth access", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Log.d("Error", "Document does not exist");
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Error", "Error getting document", e);
                                            }
                                        });


//                                String userId = auth.getCurrentUser().getUid();
//                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
//                                SharedPreferences.Editor editor = preferences.edit();
//                                editor.putString("userId", userId);
//                                editor.apply();
//
//                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                startActivity(intent);
//                                LoginActivity.this.finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    else {
                        loginPassword.setError("Password cannot be empty");
                    }
                }
                else if(email.isEmpty()){
                    loginEmail.setError("Email cannot be empty");
                }
                else{
                    loginEmail.setError("Invalid email format");
                }





            }
        });


        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });


    }
}
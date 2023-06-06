package com.devarshukani.green_collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterCollectorDetailsActivity extends AppCompatActivity {

    private EditText registerCollectorDetailsFirstName;
    private EditText registerCollectorDetailsLastName;
    private EditText registerCollectorDetailsContact;
    private Button registerCollectorDetailsButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_collector_details);

        registerCollectorDetailsFirstName = findViewById(R.id.registerCollectorDetailsFirstName);
        registerCollectorDetailsLastName = findViewById(R.id.registerCollectorDetailsLastName);
        registerCollectorDetailsContact = findViewById(R.id.registerCollectorDetailsContact);
        registerCollectorDetailsButton = findViewById(R.id.registerCollectorDetailsButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        registerCollectorDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = registerCollectorDetailsFirstName.getText().toString().trim();
                String lname = registerCollectorDetailsLastName.getText().toString().trim();
                String contact = registerCollectorDetailsContact.getText().toString().trim();

                if(fname.isEmpty()){
                    registerCollectorDetailsFirstName.setError("First Name cannot be empty");
                }
                else if(lname.isEmpty()){
                    registerCollectorDetailsLastName.setError("Last Name cannot be empty");
                }
                else if(contact.isEmpty()){
                    registerCollectorDetailsContact.setError("Contact cannot be empty");
                }
                else if(contact.length() < 10){
                    registerCollectorDetailsContact.setError("Invalid Contact Length");
                }
                else{
                    Map<String, Object> userdetails = new HashMap<>();
                    userdetails.put("fname", fname);
                    userdetails.put("lname", lname);
                    userdetails.put("contact", contact);
                    userdetails.put("usertype", "collector");
                    userdetails.put("greenpoints", 0);

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterCollectorDetailsActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                                db.collection("users").document(auth.getCurrentUser().getUid())
                                        .set(userdetails)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
//                                                Toast.makeText(getApplicationContext(), "Successfully added to firestore", Toast.LENGTH_SHORT).show();


                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Error", "Error adding document", e);
                                                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                            else{
                                Toast.makeText(RegisterCollectorDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    });

//                    Toast.makeText(RegisterUserDetailsActivity.this, auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();


                }
            }
        });



    }
}
package com.devarshukani.green_user.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterUserDetailsActivity extends AppCompatActivity {

    private EditText registerUserDetailsFirstName;
    private EditText registerUserDetailsLastName;
    private EditText registerUserDetailsContact;
    private Button registerUserDetailsButton;
    private FirebaseAuth auth;
    private  FirebaseFirestore db;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RegisterUserDetailsActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_details);

        registerUserDetailsFirstName = findViewById(R.id.registerUserDetailsFirstName);
        registerUserDetailsLastName = findViewById(R.id.registerUserDetailsLastName);
        registerUserDetailsContact = findViewById(R.id.registerUserDetailsContact);
        registerUserDetailsButton = findViewById(R.id.registerUserDetailsButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();




        registerUserDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = registerUserDetailsFirstName.getText().toString().trim();
                String lname  = registerUserDetailsLastName.getText().toString().trim();
                String contact  =  registerUserDetailsContact.getText().toString().trim();

                if(fname.isEmpty()){
                    registerUserDetailsFirstName.setError("First Name cannot be empty");
                }
                else if(lname.isEmpty()){
                    registerUserDetailsLastName.setError("Last Name cannot be empty");
                }
                else if(contact.isEmpty()){
                    registerUserDetailsContact.setError("Contact cannot be empty");
                }
                else if(contact.length() < 10){
                    registerUserDetailsContact.setError("Invalid Contact Length");
                }
                else{
                    Map<String, Object> userdetails = new HashMap<>();
                    userdetails.put("fname", fname);
                    userdetails.put("lname", lname);
                    userdetails.put("contact", contact);

                    db.collection("users").document(auth.getCurrentUser().getUid())
                            .set(userdetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Successfully added to firestore", Toast.LENGTH_SHORT).show();
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

            }
        });

    }
}
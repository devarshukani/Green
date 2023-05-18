package com.devarshukani.green_user.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editProfileFirstName;
    private EditText editProfileLastName;
    private EditText editProfileContact;
    private Button editProfileSaveButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileFirstName = findViewById(R.id.editProfileFirstName);
        editProfileLastName = findViewById(R.id.editProfileLastName);
        editProfileContact = findViewById(R.id.editProfileContact);
        editProfileSaveButton = findViewById(R.id.editProfileSaveButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String fname = documentSnapshot.getString("fname");
                            String lname = documentSnapshot.getString("lname");
                            String contact = documentSnapshot.getString("contact");

                            editProfileFirstName.setText(fname);
                            editProfileLastName.setText(lname);
                            editProfileContact.setText(contact);

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




        editProfileSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fname = editProfileFirstName.getText().toString().trim();
                String lname  = editProfileLastName.getText().toString().trim();
                String contact  =  editProfileContact.getText().toString().trim();

                if(fname.isEmpty()){
                    editProfileFirstName.setError("First Name cannot be empty");
                }
                else if(lname.isEmpty()){
                    editProfileLastName.setError("Last Name cannot be empty");
                }
                else if(contact.isEmpty()){
                    editProfileContact.setError("Contact cannot be empty");
                }
                else if(contact.length() < 10){
                    editProfileContact.setError("Invalid Contact Length");
                }
                else{
                    Map<String, Object> userdetails = new HashMap<>();
                    userdetails.put("fname", fname);
                    userdetails.put("lname", lname);
                    userdetails.put("contact", contact);

                    db.collection("users").document(auth.getCurrentUser().getUid())
                            .update(userdetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Successfully updated Firestore", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("Error", "Error updating document", e);
                                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        });
    }
}
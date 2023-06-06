package com.devarshukani.green_collector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;
    Button logoutButton, editProfileButton, redeemPointsProfileButton;
    TextView username_text, points_earned_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username_text = findViewById(R.id.username_text);
        points_earned_text = findViewById(R.id.points_earned_text);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        redeemPointsProfileButton = findViewById(R.id.redeemPointsProfileButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username_text.setText(auth.getCurrentUser().getEmail());

        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Long greenpoints = documentSnapshot.getLong("greenpoints");
                            if(greenpoints != null){
                                points_earned_text.setText(String.valueOf(greenpoints));
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

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", null);
                editor.apply();
                auth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                ProfileActivity.this.finish();
                Toast.makeText(ProfileActivity.this, "user logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
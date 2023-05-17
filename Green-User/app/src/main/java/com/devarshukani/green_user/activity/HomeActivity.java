package com.devarshukani.green_user.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.green_user.R;
import com.devarshukani.green_user.databinding.ActivityHomeBinding;
import com.devarshukani.green_user.fragment.AwarenessFragment;
import com.devarshukani.green_user.fragment.HistoryFragment;
import com.devarshukani.green_user.fragment.HomeFragment;
import com.devarshukani.green_user.fragment.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    FirebaseAuth auth;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
//        Toast.makeText(this, auth.getCurrentUser().getEmail() , Toast.LENGTH_SHORT).show();
        Log.d("Current User : ", auth.getCurrentUser().getEmail());


        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_home){
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.action_awareness) {
                replaceFragment(new AwarenessFragment());
            } else if (item.getItemId() == R.id.action_history) {
                replaceFragment(new HistoryFragment());
            } else if (item.getItemId() == R.id.action_profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();


    }
}
package com.example.barcodeqrapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private CheckBox disclaimerCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disclaimer_layout);

        disclaimerCheckbox = findViewById(R.id.disclaimer_checkbox);

        //Shared preferences stores the acceptance in system memory.
        sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        boolean isDisclaimerAccepted = sharedPreferences.getBoolean("is_disclaimer_accepted", false);
        if (isDisclaimerAccepted) {
            startMainActivity();
        }
    }

    public void onAcceptButtonClick(View view) {
        boolean isChecked = disclaimerCheckbox.isChecked();
        if (isChecked) {
            sharedPreferences.edit().putBoolean("is_disclaimer_accepted", true).apply();
            startMainActivity();
        } else {
            Toast.makeText(this, "You must accept the disclaimer to use this app", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}


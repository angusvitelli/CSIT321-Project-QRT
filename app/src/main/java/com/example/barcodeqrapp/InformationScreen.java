package com.example.barcodeqrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InformationScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_screen);
    }
    public void returnHome(View v){
        Intent intent = new Intent(InformationScreen.this, MainActivity.class);
        startActivity(intent);
    }
}
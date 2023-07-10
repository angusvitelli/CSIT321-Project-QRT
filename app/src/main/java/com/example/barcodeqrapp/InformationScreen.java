package com.example.barcodeqrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class InformationScreen extends MainActivity {

    String read="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_screen);
        if (MainActivity.certificateStatus==true){
            read+="Contained a valid QRT certificate to validate publisher :)\n\n";
        }
        else
        {
            read+="QR code did not contain a valid QRT certificate :(\n\n";
            if(MainActivity.trustHttps==true){
                read+="QR code was a Https Link :)\n\n";
            }else{read+="QR code was not Https :(\n\n";}
            if(MainActivity.noUnicode==true){
                read+="QR code does not contain homograph symbols :)\n\n";
            }else{read+="QR contains potentially malicious symbols :(\n\n";}
        }


        TextView readMore = findViewById(R.id.CheckView);
        readMore.setText(read);
    }
    public void returnHome(View v){
        Intent intent = new Intent(InformationScreen.this, MainActivity.class);
        startActivity(intent);
    }
}
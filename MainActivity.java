package com.example.barcodeqrapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.Button;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    String QRT = "";

    Boolean trustHttps;
    int trustScore = 0;
    String trustMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v-> {
            scanCode();
        });
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Adjust the volume to enable flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureEnable.class);
        barLaunch.launch(options);
    }

    private void trustURLTest() {
        Boolean sslTest = URLUtil.isHttpsUrl(QRT);
        if(sslTest){
           trustHttps = true;
           trustScore+=50;
        }else{
            trustHttps = false;
        }
    }

    private void contentsCheck() {
        boolean isValid = URLUtil.isValidUrl(QRT);
        //Checks the contents to see if QR contains a Message or URL

        if(isValid){
           // This means the QR contains URL
            trustURLTest();
            trustWeightingTest();
        }else{
           // This means the QR contains a message
        }
    }

    private void trustWeightingTest() {
        if(trustScore<50){
            trustMessage = "High Risk";
            runPopUp();
        }
        else if(trustScore>=50){
            trustMessage = "Moderate Risk";
            runPopUp();
        }
    }

    private void runPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("This QR Code is "+trustMessage+".");
        builder.setMessage(QRT);
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    ActivityResultLauncher<ScanOptions> barLaunch = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            QRT = result.getContents();
            contentsCheck();
        }
    });
}
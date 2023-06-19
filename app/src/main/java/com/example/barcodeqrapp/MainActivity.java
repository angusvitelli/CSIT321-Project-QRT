package com.example.barcodeqrapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.FrameLayout;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    String QRT = "";
    Boolean trustHttps;
    int trustScore = 0;
    String trustMessage;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Camera camera;
    private FrameLayout cameraContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraContainer = findViewById(R.id.cameraContainer);

        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v-> {
            scanCode();
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Camera permission granted, start the camera
            startCamera();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId){
            case R.id.action_scanner:
                scanCode();
                return true;
            case R.id.action_browser:
                return true;
            case R.id.action_accessibility:
                return true;
            case R.id.action_info :
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startCamera() {
        // Create an instance of the Camera
        camera = Camera.open();

        // Create a SurfaceView to display the camera preview
        SurfaceView surfaceView = new SurfaceView(this);

        // Set the layoutParams to match_parent
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Add the SurfaceView to the camera container
        cameraContainer.addView(surfaceView);

        // Get the SurfaceHolder for the SurfaceView
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        // Add a callback to handle SurfaceHolder events
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // Set the SurfaceHolder for the Camera
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // Handle surface changes, if needed
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // Release the Camera and remove the SurfaceView
                camera.stopPreview();
                camera.release();
                camera = null;
                cameraContainer.removeView(surfaceView);
            }
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

        //Detect modifications to url test
        //Detect website population
        //Create QR Code Template for QR Code Generation
        // QR consists of DATA/METADATA/TAG
    }

    private void contentsCheck() {
        boolean isValid = URLUtil.isValidUrl(QRT);
        //Checks the contents to see if QR contains a Message or URL

        if(isValid){
            // This means the QR contains URL
            trustURLTest();
            trustWeightingTest();
        }else{
            // This means the QR contains a message:: Check message contains anything potentially malicious by verifying publishers.
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
        builder.setNegativeButton("Read More", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface1, int j) {
                Intent intent = new Intent(MainActivity.this, InformationScreen.class);
                startActivity(intent);
            }
        }).show();
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the Camera if it's still open
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    ActivityResultLauncher<ScanOptions> barLaunch = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            QRT = result.getContents();
            contentsCheck();
        }
    });
}
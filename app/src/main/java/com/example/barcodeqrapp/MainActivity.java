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
import android.net.Uri;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    URI uri;
    String QRT = "";
    Boolean trustHttps;
    Boolean certificateStatus;
    Boolean noUnicode;
    String unicodeStatus;
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
                Intent i5 = new Intent(this, MainActivity.class);
                startActivity(i5);
                scanCode();
                return true;
            case R.id.action_browser:
                Intent i = new Intent(this, BrowserChange.class);
                startActivity(i);
                return true;
            case R.id.action_accessibility:
                Intent i2 = new Intent(this, Accessibility.class);
                startActivity(i2);
                return true;
            case R.id.action_info :
                Intent i3 = new Intent(this, Information.class);
                startActivity(i3);
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
    public void scanCode() {
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
            unicodeTest();
            //websitePopulationTest() TODO
            trustWeightingTest();
        }else{
            // This means the QR contains a message:: Check message contains anything potentially malicious by verifying publishers.
        }
    }

    private void trustWeightingTest() {
        if(certificateStatus)
        {
            trustMessage="This Link Is QRT Verified";
            runPopUp();
        }
        else {
            if (trustScore < 50) {
                trustMessage = "High Risk";
                runPopUp();
            } else if (trustScore >= 50) {
                trustMessage = "Moderate Risk";
                runPopUp();
            }
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
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(QRT));
                startActivity(intent);
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
            try {
                certificateCheck(); //This looks for certificate in qr metadata
            } catch (JSONException e) {
                return;
            } catch (NoSuchAlgorithmException e) {
                return;
            } catch (InvalidKeySpecException e) {
                return;
            } catch (InvalidKeyException e) {
                return;
            } catch (UnsupportedEncodingException e) {
                return;
            } catch (SignatureException e) {
                return;
            }
            //trustDatabaseTest() TODO
            contentsCheck();
        }
    });

    private void certificateCheck() throws JSONException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException, SignatureException {
        String jsonObjectString = QRT.substring(QRT.indexOf('{'), QRT.lastIndexOf('}') + 1);

        // Pass json data back into object then retrieve algorithm initialise keyfactory.
        JSONObject obj = new JSONObject(jsonObjectString);
        Signature ecdsaVerify = Signature.getInstance(obj.getString("algorithm"));
        KeyFactory kf = KeyFactory.getInstance("EC");

        //Retrieve public key from file
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(obj.getString("publicKey")));
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        //Attach the public key to verify the signature of the certificate
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(obj.getString("id").getBytes("UTF-8"));

        //Finally using signature check if the key is consistent with the contained content
        boolean valid = ecdsaVerify.verify(Base64.getDecoder().decode(obj.getString("signature")));

        if (valid==true){
            certificateStatus=true;
            trustWeightingTest();
        }
        else{
            certificateStatus=false;
            return;
        }
    }
    private void unicodeTest() {
        try {
            uri = new URI(QRT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (uri != null && uri.isOpaque()) {
            noUnicode = false;
            unicodeStatus = "This URL contains Unicode";
        } else {
            noUnicode = true;
            trustScore += 50;
            unicodeStatus = "No Unicode";
        }
    }
}
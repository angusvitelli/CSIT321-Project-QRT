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
import android.util.Log;


import java.security.Security;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

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


public class MainActivity extends AppCompatActivity {

    Button scanButton;
    URI uri;
    String QRT = "";
    static Boolean trustHttps;
    static Boolean certificateStatus = false;
    static Boolean noUnicode;
    static String unicodeStatus;
    int trustScore = 0;
    String trustMessage;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Camera camera;
    private FrameLayout cameraContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    ActivityResultLauncher<ScanOptions> barLaunch = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            QRT = result.getContents();
            try {
                certificateCheck(); //This looks for certificate in qr metadata
            } catch (JSONException e) {
            }
            //trustDatabaseTest() TODO
            contentsCheck();
        }
    });

    private void certificateCheck() throws JSONException {
        JSONObject obj = null;
        String base64PublicKey = null;
        try {
            String result = QRT;

            int startIndex = result.indexOf('{');
            // Extract the substring before the JSON object
            String substringBeforeJson = result.substring(0, startIndex);
            QRT = substringBeforeJson;

            // Extract the JSON object
            String jsonObjectString = result.substring(result.indexOf('{'), result.lastIndexOf('}') + 1);

            // Pass JSON data back into object then retrieve algorithm initialize KeyFactory.
            obj = new JSONObject(jsonObjectString);
            Signature ecdsaVerify = Signature.getInstance(obj.getString("algorithm"));
            KeyFactory kf = KeyFactory.getInstance("EC");

            //Retrieve and decode public key
            base64PublicKey = obj.getString("publicKey").replaceAll("\\s", "");
            byte[] publicKeyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT);
            // Log the byte array as a readable string representation

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

            //Generate the PublicKey object using the KeyFactory
            PublicKey publicKey = kf.generatePublic(publicKeySpec); //TODO figure out why this is not loading the string to public key

            // Attach the public key to verify the signature of the certificate
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(obj.getString("id").getBytes("UTF-8"));

            // Finally, use the signature to check if the key is consistent with the contained content
            byte[] decodedSignature = Base64.decode(obj.getString("signature"), Base64.DEFAULT);
            boolean valid = ecdsaVerify.verify(decodedSignature);

            if (valid) {
                certificateStatus = true;
                trustWeightingTest();

            } else {
                certificateStatus = false;
            }

        } catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            // Handle the exception or display an error message for debugging
            Log.e("CertificateCheck", "Exception occurred", e);
            Log.e("publicKey", base64PublicKey);
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
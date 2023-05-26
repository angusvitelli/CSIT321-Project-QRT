package com.example.barcodeqrapp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Button scanButton;
    String QRT = "";

    static boolean trustHttps;
    static boolean trustSignature;
    static boolean noUnicode;
    int trustScore = 0;
    String trustMessage;
    URI uri = null;

    public MainActivity() throws IOException, NotFoundException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = findViewById(R.id.scanButton);
        ImageView logo = findViewById(R.id.logoImage);

        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                logo.setVisibility(View.VISIBLE);
                scanButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        logo.startAnimation(fadeIn);
        scanButton.startAnimation(fadeIn);

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

    //detect URL modifications (unicode)
    private void unicodeTest() {
        try {
            uri = new URI(QRT);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (uri != null && uri.isOpaque()) {
            noUnicode = false;
        } else {
            noUnicode = true;
            trustScore += 50;
        }
    }

    //detect website population
    //can be done with web scraping if deemed necessary (not very legal in some circumstances)

    //decode QR code and validate the certificate

    private void trustURLTest() {
        Boolean sslTest = URLUtil.isHttpsUrl(QRT);
        if (sslTest) {
            trustHttps = true;
            trustScore += 50;
        } else {
            trustHttps = false;
        }
    }

    private void contentsCheck() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        boolean isValid = URLUtil.isValidUrl(QRT);
        //Checks the contents to see if QR contains a Message or URL

        if (isValid) {
            // This means the QR contains URL
            trustSignature();
            trustURLTest();
            trustWeightingTest();
            unicodeTest();
        } else {
            trustSignature();
        }
    }

    private void trustSignature(){
        try{
            // Extract the JSON object
            String jsonObjectString = QRT.substring(QRT.indexOf('{'), QRT.lastIndexOf('}') + 1);

            // Pass json data back into object then retrieve algorithm initialise keyfactory.
            JSONObject obj = new JSONObject(jsonObjectString);
            Signature ecdsaVerify = Signature.getInstance(obj.getString("algorithm"));
            KeyFactory kf = KeyFactory.getInstance("EC");

            //Retrieve public key from file
            EncodedKeySpec publicKeySpec = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(obj.getString("publicKey")));
            }
            PublicKey publicKey = kf.generatePublic(publicKeySpec);
            //Attach the public key to verify the signature of the certificate
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(obj.getString("id").getBytes("UTF-8"));

            //Finally using signature check if the key is consistent with the contained content
            boolean valid;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                valid = ecdsaVerify.verify(Base64.getDecoder().decode(obj.getString("signature")));
            }
            trustSignature = true;
            trustScore = trustScore + 100;

        } catch (JSONException e) {
            trustSignature = false;
        } catch (UnsupportedEncodingException e) {
            trustSignature = false;
        } catch (NoSuchAlgorithmException e) {
            trustSignature = false;
        } catch (InvalidKeySpecException e) {
            trustSignature = false;
        } catch (SignatureException e) {
            trustSignature = false;
        } catch (InvalidKeyException e) {
            trustSignature = false;
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

    ActivityResultLauncher<ScanOptions> barLaunch = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            QRT = result.getContents();
            try {
                contentsCheck();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
    });
}
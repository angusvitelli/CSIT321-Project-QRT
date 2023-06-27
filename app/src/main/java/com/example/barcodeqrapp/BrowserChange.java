package com.example.barcodeqrapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BrowserChange extends MainActivity {
    TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_change);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        t=findViewById(R.id.textView4);
        textSetter();

    }

    private void textSetter() {
        String browser = PreferenceUtil.getDefaultBrowser(this);
        if(browser.equals("com.android.chrome"))
        {
            t.setText("Google Chrome");
        }
        if(browser.equals("org.mozilla.firefox"))
        {
            t.setText("Mozilla Firefox");
        }
        if(browser.equals("com.opera.browser"))
        {
            t.setText("Opera");
        }
    }

    public void returnHome(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void doChange(View v){
        Button b = findViewById(v.getId());
        String browserId = b.getText().toString();
        if(browserId.equals("Chrome"))
        {
            String selectedBrowserPackage = "com.android.chrome";
            PreferenceUtil.setDefaultBrowser(getApplicationContext(), selectedBrowserPackage);
            Toast.makeText(getApplicationContext(), "Default browser set to " + browserId, Toast.LENGTH_SHORT).show();
        }
        if(browserId.equals("Firefox"))
        {
            String selectedBrowserPackage = "org.mozilla.firefox";
            PreferenceUtil.setDefaultBrowser(getApplicationContext(), selectedBrowserPackage);
            Toast.makeText(getApplicationContext(), "Default browser set to " + browserId, Toast.LENGTH_SHORT).show();
        }
        if(browserId.equals("Opera"))
        {
            String selectedBrowserPackage = "com.opera.browser";
            PreferenceUtil.setDefaultBrowser(getApplicationContext(), selectedBrowserPackage);
            Toast.makeText(getApplicationContext(), "Default browser set to " + browserId, Toast.LENGTH_SHORT).show();
        }
        textSetter();
    }

}

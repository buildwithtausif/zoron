package com.zoron.whyred;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvStatus = findViewById(R.id.tvSplashStatus);

        new Thread(() -> {
            try {
                // Request root access
                Process p = Runtime.getRuntime().exec("su -c id");
                p.waitFor();
                
                if (p.exitValue() == 0) {
                    runOnUiThread(() -> {
                        tvStatus.setText("Root Granted!");
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        tvStatus.setText("Root Access Denied!");
                        Toast.makeText(SplashActivity.this, "This app requires Root (Magisk).", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }
}

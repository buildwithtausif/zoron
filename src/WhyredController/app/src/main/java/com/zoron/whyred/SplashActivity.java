package com.zoron.whyred;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_TIME_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvBrand = findViewById(R.id.tvSplashBrand);
        TextView tvSubtitle = findViewById(R.id.tvSplashSubtitle);
        View progress = findViewById(R.id.splashProgress);
        TextView tvStatus = findViewById(R.id.tvSplashStatus);

        // Set initial state
        tvBrand.setAlpha(0f);
        tvSubtitle.setAlpha(0f);
        progress.setAlpha(0f);
        tvStatus.setAlpha(0f);

        // Run staggered fade-in animations
        tvBrand.animate().alpha(1f).setDuration(500).setStartDelay(0).start();
        tvSubtitle.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
        progress.animate().alpha(1f).setDuration(500).setStartDelay(400).start();
        tvStatus.animate().alpha(1f).setDuration(500).setStartDelay(600).start();

        long startTime = System.currentTimeMillis();

        new Thread(() -> {
            boolean rootGranted = false;
            String errorMessage = null;

            try {
                // Request root access
                Process p = Runtime.getRuntime().exec("su -c id");
                p.waitFor();
                rootGranted = (p.exitValue() == 0);
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }

            final boolean success = rootGranted;
            final String err = errorMessage;

            // Calculate remaining time to show splash screen
            long elapsedTime = System.currentTimeMillis() - startTime;
            long delay = Math.max(0, MIN_SPLASH_TIME_MS - elapsedTime);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // Ignore
            }

            runOnUiThread(() -> {
                android.content.SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
                prefs.edit().putBoolean("is_root", success).apply();

                if (success) {
                    tvStatus.setText("Root Granted!");
                } else {
                    tvStatus.setText("Non-Root Mode");
                    Toast.makeText(SplashActivity.this, "Running in Non-Root Fallback Mode", Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }).start();
    }
}

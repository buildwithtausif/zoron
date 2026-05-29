package com.zoron.whyred;

import android.graphics.Color;
import java.io.File;
import android.content.SharedPreferences;
import com.topjohnwu.superuser.Shell;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ModeLearnActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_learn);

        androidx.compose.ui.platform.ComposeView composeView = findViewById(R.id.compose_view);
        com.zoron.whyred.ui.ComposeInterop.setModeLearnContent(composeView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyAmoledThemeBasedOnMode();
    }

    private void applyAmoledThemeBasedOnMode() {
        SharedPreferences preferences = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = preferences.getBoolean("is_root", false);
        String currentMode = "balanced";
        if (isRoot) {
            Shell.Result r = Shell.cmd("cat /data/local/tmp/zoron/profile.txt 2>/dev/null").exec();
            if (r.isSuccess() && !r.getOut().isEmpty()) {
                currentMode = r.getOut().get(0).trim().toLowerCase();
            }
        } else {
            File zoronDir = new File(getFilesDir(), "zoron");
            File profileFile = new File(zoronDir, "profile.txt");
            if (profileFile.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                    String m = br.readLine();
                    if (m != null) currentMode = m.trim().toLowerCase();
                } catch (Exception ignored) {}
            }
        }
        boolean isDeepProfile = "deep".equals(currentMode) || "hibernation".equals(currentMode) || "nightwatch".equals(currentMode);
        applyAmoledTheme(isDeepProfile);
    }

    private void applyAmoledTheme(boolean amoledActive) {
        ViewGroup rootLayout = null;
        ViewGroup content = findViewById(android.R.id.content);
        if (content != null && content.getChildCount() > 0) {
            View firstChild = content.getChildAt(0);
            if (firstChild instanceof ViewGroup) {
                rootLayout = (ViewGroup) firstChild;
            }
        }
        if (rootLayout != null) {
            if (amoledActive) {
                rootLayout.setBackgroundColor(Color.BLACK);
            } else {
                rootLayout.setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_gradient_main));
            }
        }
    }

    private void animateStaggeredEntry() {
        // Obsolete in Compose
    }
}

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

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Run entry animations
        animateStaggeredEntry();
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
        View scrollView = findViewById(android.R.id.content);
        if (scrollView == null) return;

        ViewGroup rootContent = (ViewGroup) ((ViewGroup) scrollView).getChildAt(0); // CoordinatorLayout
        if (rootContent == null || rootContent.getChildCount() < 2) return;

        // The NestedScrollView is the second child (after AppBarLayout)
        View nestedScroll = rootContent.getChildAt(1);
        if (!(nestedScroll instanceof ViewGroup)) return;

        ViewGroup scrollContent = (ViewGroup) nestedScroll;
        if (scrollContent.getChildCount() == 0) return;

        // The LinearLayout inside NestedScrollView
        View innerLayout = scrollContent.getChildAt(0);
        if (!(innerLayout instanceof ViewGroup)) return;

        ViewGroup mainLayout = (ViewGroup) innerLayout;
        int childCount = mainLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = mainLayout.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(60f);
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(i * 60L)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .start();
        }
    }
}

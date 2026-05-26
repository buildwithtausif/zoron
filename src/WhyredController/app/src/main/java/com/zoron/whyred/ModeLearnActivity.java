package com.zoron.whyred;

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

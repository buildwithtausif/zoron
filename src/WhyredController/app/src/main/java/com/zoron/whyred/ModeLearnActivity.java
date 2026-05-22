package com.zoron.whyred;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ModeLearnActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_learn);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
    }
}

package com.zoron.whyred;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.topjohnwu.superuser.Shell;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerLogLevel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        spinnerLogLevel = findViewById(R.id.spinnerLogLevel);
        String[] levels = {"NORMAL", "DEBUG", "TRACE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLogLevel.setAdapter(adapter);

        // Load current
        Shell.cmd("cat /data/local/tmp/zoron/log_level.txt").submit(out -> {
            runOnUiThread(() -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    String level = out.getOut().get(0).trim();
                    for (int i = 0; i < levels.length; i++) {
                        if (levels[i].equals(level)) {
                            spinnerLogLevel.setSelection(i);
                            break;
                        }
                    }
                }
            });
        });

        spinnerLogLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = levels[position];
                Shell.cmd("echo " + selected + " > /data/local/tmp/zoron/log_level.txt").exec();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}

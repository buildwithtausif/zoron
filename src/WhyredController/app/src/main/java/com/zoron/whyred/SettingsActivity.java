package com.zoron.whyred;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        // Load current log level
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

        // Fastpath toggle
        com.google.android.material.materialswitch.MaterialSwitch switchFastpath = findViewById(R.id.switchFastpath);
        Shell.cmd("cat /data/local/tmp/zoron/fastpath_enabled.txt 2>/dev/null || echo '1'").submit(out -> {
            runOnUiThread(() -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    String val = out.getOut().get(0).trim();
                    switchFastpath.setChecked(!"0".equals(val));
                }
            });
        });
        switchFastpath.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            Shell.cmd("echo " + val + " > /data/local/tmp/zoron/fastpath_enabled.txt").exec();
        });

        // Adaptive Learning toggle
        com.google.android.material.materialswitch.MaterialSwitch switchAdaptiveLearning = findViewById(R.id.switchAdaptiveLearning);
        Shell.cmd("cat /data/local/tmp/zoron/adaptive_learning.txt 2>/dev/null || echo '1'").submit(out -> {
            runOnUiThread(() -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    String val = out.getOut().get(0).trim();
                    switchAdaptiveLearning.setChecked(!"0".equals(val));
                }
            });
        });
        switchAdaptiveLearning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            Shell.cmd("echo " + val + " > /data/local/tmp/zoron/adaptive_learning.txt").exec();
        });

        // Developer Mode toggle
        com.google.android.material.materialswitch.MaterialSwitch switchDevMode = findViewById(R.id.switchDevMode);
        Shell.cmd("cat /data/local/tmp/zoron/dev_mode.txt 2>/dev/null || echo '0'").submit(out -> {
            runOnUiThread(() -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    String val = out.getOut().get(0).trim();
                    switchDevMode.setChecked("1".equals(val));
                }
            });
        });
        switchDevMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            Shell.cmd("echo " + val + " > /data/local/tmp/zoron/dev_mode.txt").exec();
        });

        // Export Logs Button
        com.google.android.material.materialswitch.MaterialSwitch switchZipExport = findViewById(R.id.switchZipExport);
        findViewById(R.id.btnSettingsExportLogs).setOnClickListener(v -> {
            boolean zipIt = switchZipExport.isChecked();
            exportLogs(zipIt);
        });

        // Clear Logs & Analytics
        findViewById(R.id.btnSettingsClearLogs).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Data")
                .setMessage("Are you sure you want to delete all logs and analytics data? This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    Shell.cmd("rm -f /data/local/tmp/zoron/*.txt /data/local/tmp/zoron/*.csv /data/local/tmp/zoron/*.dat").submit(out -> {
                        runOnUiThread(() -> Toast.makeText(this, "Logs & Analytics Cleared", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Run entry animations
        animateStaggeredEntry();
    }

    private void exportLogs(boolean zipIt) {
        new Thread(() -> {
            try {
                File exportFile;
                String mimeType;
                
                if (zipIt) {
                    exportFile = new File(getExternalCacheDir(), "zoron_logs.zip");
                    mimeType = "application/zip";
                    
                    // Copy files to cache dir first because /data/local/tmp is root-only
                    File tempDir = new File(getExternalCacheDir(), "temp_logs");
                    tempDir.mkdirs();
                    Shell.cmd("cp /data/local/tmp/zoron/* " + tempDir.getAbsolutePath() + "/").exec();
                    Shell.cmd("chmod 666 " + tempDir.getAbsolutePath() + "/*").exec();
                    
                    FileOutputStream fos = new FileOutputStream(exportFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    
                    File[] files = tempDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (!file.isFile()) continue;
                            FileInputStream fis = new FileInputStream(file);
                            ZipEntry zipEntry = new ZipEntry(file.getName());
                            zos.putNextEntry(zipEntry);
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = fis.read(bytes)) >= 0) {
                                zos.write(bytes, 0, length);
                            }
                            zos.closeEntry();
                            fis.close();
                            file.delete();
                        }
                    }
                    zos.close();
                    fos.close();
                    tempDir.delete();
                } else {
                    exportFile = new File(getExternalCacheDir(), "zoron_diagnostics.txt");
                    mimeType = "text/plain";
                    Shell.cmd("cat /data/local/tmp/zoron/log.txt > " + exportFile.getAbsolutePath()).exec();
                }

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", exportFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType(mimeType);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Zoron Logs Export");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                new Handler(Looper.getMainLooper()).post(() -> startActivity(Intent.createChooser(shareIntent, "Export Logs")));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
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

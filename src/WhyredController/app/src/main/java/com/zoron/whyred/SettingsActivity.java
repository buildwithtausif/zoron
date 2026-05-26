package com.zoron.whyred;

import android.graphics.Color;
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
        boolean isRoot = prefs.getBoolean("is_root", false);

        if (isRoot) {
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
        } else {
            String level = getLocalConfigValue("log_level.txt", "NORMAL");
            for (int i = 0; i < levels.length; i++) {
                if (levels[i].equals(level)) {
                    spinnerLogLevel.setSelection(i);
                    break;
                }
            }
        }

        spinnerLogLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = levels[position];
                if (isRoot) {
                    Shell.cmd("echo " + selected + " > /data/local/tmp/zoron/log_level.txt").exec();
                } else {
                    writeLocalConfigValue("log_level.txt", selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Fastpath toggle
        com.google.android.material.materialswitch.MaterialSwitch switchFastpath = findViewById(R.id.switchFastpath);
        if (isRoot) {
            Shell.cmd("cat /data/local/tmp/zoron/fastpath_enabled.txt 2>/dev/null || echo '1'").submit(out -> {
                runOnUiThread(() -> {
                    if (out.isSuccess() && !out.getOut().isEmpty()) {
                        String val = out.getOut().get(0).trim();
                        switchFastpath.setChecked(!"0".equals(val));
                    }
                });
            });
        } else {
            String val = getLocalConfigValue("fastpath_enabled.txt", "1");
            switchFastpath.setChecked(!"0".equals(val));
        }
        switchFastpath.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            if (isRoot) {
                Shell.cmd("echo " + val + " > /data/local/tmp/zoron/fastpath_enabled.txt").exec();
            } else {
                writeLocalConfigValue("fastpath_enabled.txt", val);
            }
        });

        // Adaptive Learning toggle
        com.google.android.material.materialswitch.MaterialSwitch switchAdaptiveLearning = findViewById(R.id.switchAdaptiveLearning);
        if (isRoot) {
            Shell.cmd("cat /data/local/tmp/zoron/adaptive_learning.txt 2>/dev/null || echo '1'").submit(out -> {
                runOnUiThread(() -> {
                    if (out.isSuccess() && !out.getOut().isEmpty()) {
                        String val = out.getOut().get(0).trim();
                        switchAdaptiveLearning.setChecked(!"0".equals(val));
                    }
                });
            });
        } else {
            String val = getLocalConfigValue("adaptive_learning.txt", "1");
            switchAdaptiveLearning.setChecked(!"0".equals(val));
        }
        switchAdaptiveLearning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            if (isRoot) {
                Shell.cmd("echo " + val + " > /data/local/tmp/zoron/adaptive_learning.txt").exec();
            } else {
                writeLocalConfigValue("adaptive_learning.txt", val);
            }
        });

        // Developer Mode toggle
        com.google.android.material.materialswitch.MaterialSwitch switchDevMode = findViewById(R.id.switchDevMode);
        if (isRoot) {
            Shell.cmd("cat /data/local/tmp/zoron/dev_mode.txt 2>/dev/null || echo '0'").submit(out -> {
                runOnUiThread(() -> {
                    if (out.isSuccess() && !out.getOut().isEmpty()) {
                        String val = out.getOut().get(0).trim();
                        switchDevMode.setChecked("1".equals(val));
                    }
                });
            });
        } else {
            String val = getLocalConfigValue("dev_mode.txt", "0");
            switchDevMode.setChecked("1".equals(val));
        }
        switchDevMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String val = isChecked ? "1" : "0";
            if (isRoot) {
                Shell.cmd("echo " + val + " > /data/local/tmp/zoron/dev_mode.txt").exec();
            } else {
                writeLocalConfigValue("dev_mode.txt", val);
            }
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
                    if (isRoot) {
                        Shell.cmd("rm -f /data/local/tmp/zoron/*.txt /data/local/tmp/zoron/*.csv /data/local/tmp/zoron/*.dat").submit(out -> {
                            runOnUiThread(() -> Toast.makeText(this, "Logs & Analytics Cleared", Toast.LENGTH_SHORT).show());
                        });
                    } else {
                        try {
                            File zoronDir = new File(getFilesDir(), "zoron");
                            if (zoronDir.exists()) {
                                File[] files = zoronDir.listFiles();
                                if (files != null) {
                                    for (File f : files) f.delete();
                                }
                            }
                            Toast.makeText(this, "Logs & Analytics Cleared", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error clearing logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        // Developer attribution click listener
        View devAttribution = findViewById(R.id.cardSettingsDeveloperAttribution);
        if (devAttribution != null) {
            devAttribution.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/buildwithtausif/zoron"));
                startActivity(intent);
            });
        }

        // Run entry animations
        animateStaggeredEntry();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyAmoledThemeBasedOnMode();
    }

    private void applyAmoledThemeBasedOnMode() {
        android.content.SharedPreferences preferences = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
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

    private void exportLogs(boolean zipIt) {
        android.content.SharedPreferences preferences = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = preferences.getBoolean("is_root", false);

        new Thread(() -> {
            try {
                File exportFile;
                String mimeType;
                
                if (zipIt) {
                    exportFile = new File(getExternalCacheDir(), "zoron_logs.zip");
                    mimeType = "application/zip";
                    
                    FileOutputStream fos = new FileOutputStream(exportFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);

                    if (isRoot) {
                        // Copy files to cache dir first because /data/local/tmp is root-only
                        File tempDir = new File(getExternalCacheDir(), "temp_logs");
                        tempDir.mkdirs();
                        Shell.cmd("cp /data/local/tmp/zoron/* " + tempDir.getAbsolutePath() + "/").exec();
                        Shell.cmd("chmod 666 " + tempDir.getAbsolutePath() + "/*").exec();
                        
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
                        tempDir.delete();
                    } else {
                        // Zip directly from local sandbox
                        File zoronDir = new File(getFilesDir(), "zoron");
                        if (zoronDir.exists()) {
                            File[] files = zoronDir.listFiles();
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
                                }
                            }
                        }
                    }
                    zos.close();
                    fos.close();
                } else {
                    exportFile = new File(getExternalCacheDir(), "zoron_diagnostics.txt");
                    mimeType = "text/plain";
                    if (isRoot) {
                        Shell.cmd("cat /data/local/tmp/zoron/log.txt > " + exportFile.getAbsolutePath()).exec();
                    } else {
                        // Copy local log file directly
                        File logFile = new File(new File(getFilesDir(), "zoron"), "log.txt");
                        if (logFile.exists()) {
                            try (FileInputStream fis = new FileInputStream(logFile);
                                 FileOutputStream out = new FileOutputStream(exportFile)) {
                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = fis.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }
                            }
                        } else {
                            try (FileOutputStream out = new FileOutputStream(exportFile)) {
                                out.write("No logs yet.".getBytes());
                            }
                        }
                    }
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

    private String getLocalConfigValue(String filename, String defaultValue) {
        File file = new File(new File(getFilesDir(), "zoron"), filename);
        if (file.exists()) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String val = br.readLine();
                if (val != null) return val.trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    private void writeLocalConfigValue(String filename, String value) {
        try {
            File zoronDir = new File(getFilesDir(), "zoron");
            if (!zoronDir.exists()) zoronDir.mkdirs();
            File file = new File(zoronDir, filename);
            java.io.FileWriter fw = new java.io.FileWriter(file);
            fw.write(value);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
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

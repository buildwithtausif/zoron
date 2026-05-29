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

        androidx.compose.ui.platform.ComposeView composeView = findViewById(R.id.compose_view_settings);
        com.zoron.whyred.ui.ComposeInterop.setSettingsContent(composeView);
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
                        
                        com.zoron.whyred.data.DatabaseExporter.exportDatabaseToCsv(SettingsActivity.this, tempDir);
                        
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
                        com.zoron.whyred.data.DatabaseExporter.exportDatabaseToCsv(SettingsActivity.this, zoronDir);
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
                                    if (file.getName().startsWith("db_export_")) {
                                        file.delete();
                                    }
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
        // Obsolete in Compose
    }
}

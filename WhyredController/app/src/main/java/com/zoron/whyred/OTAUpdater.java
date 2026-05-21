package com.zoron.whyred;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OTAUpdater {
    // Official GitHub Raw URL for the unified module update
    private static final String OTA_URL = "https://raw.githubusercontent.com/buildwithtausif/zoron/main/update.json";
    private static final int CURRENT_VERSION_CODE = 9; // v2.9.3

    public static void checkUpdates(Activity activity, boolean manualCheck) {
        new Thread(() -> {
            try {
                URL url = new URL(OTA_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonStr.append(line);
                }
                
                JSONObject json = new JSONObject(jsonStr.toString());
                int latestVersionCode = json.getInt("versionCode");
                String latestVersionName = json.getString("version");
                String zipUrl = json.getString("zipUrl");
                String changelogText = json.has("changelog") ? json.getString("changelog") : "New update available.";
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (latestVersionCode > CURRENT_VERSION_CODE) {
                        new AlertDialog.Builder(activity)
                            .setTitle("Update Available: " + latestVersionName)
                            .setMessage("A new Magisk Module update is available!\n\n" + changelogText + "\n\nThis will automatically download and flash the module, including the latest app update.")
                            .setPositiveButton("Download & Install", (dialog, which) -> {
                                downloadAndFlashUpdate(activity, zipUrl);
                            })
                            .setNegativeButton("Later", null)
                            .show();
                    } else if (manualCheck) {
                        Toast.makeText(activity, "Zoron is up to date! (" + latestVersionName + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                if (manualCheck) {
                    new Handler(Looper.getMainLooper()).post(() -> 
                        Toast.makeText(activity, "Failed to check for updates: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        }).start();
    }

    private static void downloadAndFlashUpdate(Activity activity, String zipUrl) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Downloading Update");
        progressDialog.setMessage("Please wait while the update is downloading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(zipUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                
                File outputFile = new File(activity.getCacheDir(), "zoron_update.zip");
                FileOutputStream output = new FileOutputStream(outputFile);

                byte[] data = new byte[8192];
                long total = 0;
                int count;
                
                while ((count = input.read(data)) != -1) {
                    total += count;
                    if (fileLength > 0) {
                        int progress = (int) (total * 100 / fileLength);
                        new Handler(Looper.getMainLooper()).post(() -> progressDialog.setProgress(progress));
                    }
                    output.write(data, 0, count);
                }
                
                output.flush();
                output.close();
                input.close();

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.setMessage("Flashing Magisk Module...");
                    progressDialog.setIndeterminate(true);
                });

                // Flash via Magisk natively using libsu
                Shell.Result result = Shell.cmd("magisk --install-module " + outputFile.getAbsolutePath()).exec();

                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.dismiss();
                    
                    if (result.isSuccess()) {
                        new AlertDialog.Builder(activity)
                            .setTitle("Update Successful")
                            .setMessage("The module has been successfully flashed. You must reboot your device to apply the new module and app update.")
                            .setPositiveButton("Reboot Now", (dialog, which) -> {
                                Shell.cmd("reboot").exec();
                            })
                            .setNegativeButton("Later", null)
                            .setCancelable(false)
                            .show();
                    } else {
                        StringBuilder err = new StringBuilder("Flash Error:\n");
                        for (String s : result.getErr()) err.append(s).append("\n");
                        for (String s : result.getOut()) err.append(s).append("\n");
                        
                        new AlertDialog.Builder(activity)
                            .setTitle("Update Failed")
                            .setMessage(err.toString())
                            .setPositiveButton("OK", null)
                            .show();
                    }
                });
                
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(activity, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}

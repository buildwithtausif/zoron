package com.zoron.whyred;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.topjohnwu.superuser.Shell;
import com.zoron.whyred.ui.ComposeState;

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
    private static final String OTA_URL = "https://raw.githubusercontent.com/buildwithtausif/zoron/main/update.json";

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
                
                // Fetch the full changelog history from github
                String changelogText = "No release history available.";
                try {
                    URL changelogUrl = new URL("https://raw.githubusercontent.com/buildwithtausif/zoron/main/changelog.md");
                    HttpURLConnection clConn = (HttpURLConnection) changelogUrl.openConnection();
                    clConn.setRequestMethod("GET");
                    clConn.setConnectTimeout(5000);
                    BufferedReader clReader = new BufferedReader(new InputStreamReader(clConn.getInputStream()));
                    StringBuilder clBuilder = new StringBuilder();
                    String clLine;
                    while ((clLine = clReader.readLine()) != null) {
                        clBuilder.append(clLine).append("\n");
                    }
                    if (clBuilder.length() > 0) {
                        changelogText = clBuilder.toString();
                    }
                } catch (Exception e) {
                    // fallback to json's simple changelog if available
                    changelogText = json.has("changelog") ? json.getString("changelog") : "New update available.";
                }

                final String finalChangelog = changelogText;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (latestVersionCode > BuildConfig.VERSION_CODE) {
                        ComposeState.INSTANCE.getOtaVersion().setValue(latestVersionName);
                        ComposeState.INSTANCE.getOtaChangelog().setValue(finalChangelog);
                        ComposeState.INSTANCE.getOtaZipUrl().setValue(zipUrl);
                        ComposeState.INSTANCE.getOtaAvailable().setValue(true);
                        ComposeState.INSTANCE.getShowOtaDialog().setValue(true);
                    } else if (manualCheck) {
                        ComposeState.INSTANCE.getOtaAvailable().setValue(false);
                        ComposeState.INSTANCE.getOtaVersion().setValue(latestVersionName + " (Up to date)");
                        ComposeState.INSTANCE.getOtaChangelog().setValue(finalChangelog);
                        ComposeState.INSTANCE.getShowOtaDialog().setValue(true);
                    } else {
                        // Even if not manual check, we want the changelog available for the "What's New" page
                        ComposeState.INSTANCE.getOtaVersion().setValue(latestVersionName);
                        ComposeState.INSTANCE.getOtaChangelog().setValue(finalChangelog);
                    }
                });
            } catch (Exception e) {
                if (manualCheck) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ComposeState.INSTANCE.getOtaVersion().setValue("Error");
                        ComposeState.INSTANCE.getOtaChangelog().setValue("Failed to check for updates: " + e.getMessage());
                        ComposeState.INSTANCE.getShowOtaDialog().setValue(true);
                    });
                }
            }
        }).start();
    }

    public static void downloadAndFlashUpdate(Activity activity, String zipUrl) {
        ComposeState.INSTANCE.getOtaDownloading().setValue(true);
        ComposeState.INSTANCE.getOtaDownloadProgress().setValue(0f);
        ComposeState.INSTANCE.getOtaFlashing().setValue(false);
        ComposeState.INSTANCE.getOtaFlashResult().setValue("");

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
                        float progress = (float) total / fileLength;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            ComposeState.INSTANCE.getOtaDownloadProgress().setValue(progress);
                        });
                    }
                    output.write(data, 0, count);
                }
                
                output.flush();
                output.close();
                input.close();

                new Handler(Looper.getMainLooper()).post(() -> {
                    ComposeState.INSTANCE.getOtaDownloading().setValue(false);
                    ComposeState.INSTANCE.getOtaFlashing().setValue(true);
                });

                // Flash via Magisk natively using libsu
                Shell.Result result = Shell.cmd("magisk --install-module " + outputFile.getAbsolutePath()).exec();

                new Handler(Looper.getMainLooper()).post(() -> {
                    ComposeState.INSTANCE.getOtaFlashing().setValue(false);
                    if (result.isSuccess()) {
                        ComposeState.INSTANCE.getOtaFlashSuccess().setValue(true);
                        ComposeState.INSTANCE.getOtaFlashResult().setValue("The module has been successfully flashed. Reboot your device to apply the update.");
                    } else {
                        ComposeState.INSTANCE.getOtaFlashSuccess().setValue(false);
                        StringBuilder err = new StringBuilder("Flash Error:\n");
                        for (String s : result.getErr()) err.append(s).append("\n");
                        for (String s : result.getOut()) err.append(s).append("\n");
                        ComposeState.INSTANCE.getOtaFlashResult().setValue(err.toString());
                    }
                });
                
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ComposeState.INSTANCE.getOtaDownloading().setValue(false);
                    ComposeState.INSTANCE.getOtaFlashing().setValue(false);
                    ComposeState.INSTANCE.getOtaFlashSuccess().setValue(false);
                    ComposeState.INSTANCE.getOtaFlashResult().setValue("Download failed: " + e.getMessage());
                });
            }
        }).start();
    }
}

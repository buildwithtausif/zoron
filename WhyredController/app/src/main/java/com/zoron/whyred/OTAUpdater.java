package com.zoron.whyred;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OTAUpdater {
    // Official GitHub Raw URL
    private static final String OTA_URL = "https://raw.githubusercontent.com/buildwithtausif/zoron/main/ota.json";
    private static final int CURRENT_VERSION_CODE = 4; // v2.7.0

    public static void checkUpdates(Activity activity) {
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
                String latestVersionName = json.getString("versionName");
                String apkUrl = json.getString("apkUrl");
                String changelog = json.getString("changelog");
                
                if (latestVersionCode > CURRENT_VERSION_CODE) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        new AlertDialog.Builder(activity)
                            .setTitle("Update Available: " + latestVersionName)
                            .setMessage("A new OTA update is available!\n\nChangelog:\n" + changelog)
                            .setPositiveButton("Download", (dialog, which) -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                                activity.startActivity(browserIntent);
                            })
                            .setNegativeButton("Later", null)
                            .show();
                    });
                }
            } catch (Exception e) {
                // Ignore OTA errors silently if offline or URL is mock
            }
        }).start();
    }
}

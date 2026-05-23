package com.zoron.whyred;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.topjohnwu.superuser.Shell;

public class ZoronAutopilotService extends Service {
    private Handler handler;
    private Runnable autopilotTask;
    private String lastMode = "";

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        autopilotTask = new Runnable() {
            @Override
            public void run() {
                evaluateAndSwitchMode();
                handler.postDelayed(this, 10000); // Check every 10 seconds
            }
        };
        handler.post(autopilotTask);
        return START_STICKY;
    }

    private void evaluateAndSwitchMode() {
        // 1. Get Battery Level & Charging State
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        boolean isCharging = false;
        int batteryLevel = 50;
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            isCharging = bm.isCharging();
        }

        // 2. Get Foreground App
        String fgApp = getForegroundApp();
        String targetMode = "balanced";

        if (fgApp == null || fgApp.isEmpty()) {
            targetMode = "deep"; // Screen likely off or idle
        } else {
            // Context Detection Heuristics
            if (fgApp.contains("youtube") || fgApp.contains("netflix") || fgApp.contains("video")) {
                targetMode = "balanced"; // Video playback
            } else if (fgApp.contains("pubg") || fgApp.contains("mihoyo") || fgApp.contains("game") || fgApp.contains("roblox") || fgApp.contains("epicgames")) {
                targetMode = "burst"; // Heavy gaming
            } else if (fgApp.contains("launcher") || fgApp.contains("systemui")) {
                targetMode = "deep"; // Idle at home screen
            } else if (batteryLevel < 20 && !isCharging) {
                targetMode = "nightwatch"; // Low battery preservation
            } else if (isCharging) {
                targetMode = "balanced"; // Allow cleanup operations
            } else {
                targetMode = "balanced"; // Default social media / web
            }
        }

        if (!targetMode.equals(lastMode)) {
            // Apply new mode asynchronously with delta-based execution
            String finalTargetMode = targetMode;
            Shell.cmd("cat /data/local/tmp/zoron/profile.txt").submit(out -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    String current = out.getOut().get(0).trim();
                    if (!current.equals(finalTargetMode)) {
                        applyZoronMode(finalTargetMode);
                        lastMode = finalTargetMode;
                    }
                }
            });
        }
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return "";
        long time = System.currentTimeMillis();
        UsageEvents usageEvents = usm.queryEvents(time - 10000, time);
        UsageEvents.Event event = new UsageEvents.Event();
        String currentApp = "";
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.getPackageName();
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED && event.getPackageName().equals(currentApp)) {
                currentApp = "";
            }
        }
        return currentApp;
    }

    private void applyZoronMode(String mode) {
        // Fastpath + Engine
        Shell.cmd(
            "sh /system/bin/zoron_fastpath.sh set_mode " + mode,
            "sh /system/bin/zoron_engine " + mode + " &"
        ).exec();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && autopilotTask != null) {
            handler.removeCallbacks(autopilotTask);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

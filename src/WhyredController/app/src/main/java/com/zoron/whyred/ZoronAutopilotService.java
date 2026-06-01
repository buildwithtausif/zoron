package com.zoron.whyred;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ZoronAutopilotService extends Service {
    private Handler handler;
    private Runnable autopilotTask;
    private String lastMode = "";
    private boolean isVideoBoostActive = false;
    private long lastBatteryLogTime = 0;
    private long lastProcessReportTime = 0;
    private int audioActiveTicks = 0;
    private long lastSwitchTime = 0;
    private int modeFlapCount = 0;
    private String flapLockMode = null;
    private long flapLockExpireTime = 0;
    private String lastDetectedManualMode = "";
    private AdaptiveLearningManager learningManager;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        learningManager = new AdaptiveLearningManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("apply_mode")) {
            String mode = intent.getStringExtra("apply_mode");
            if (mode != null) {
                applyZoronMode(mode);
            }
        }
        if (autopilotTask == null) {
            autopilotTask = new Runnable() {
                @Override
                public void run() {
                    evaluateAndSwitchMode();
                    
                    // Throttle polling interval when in battery saver modes on non-root
                    int delay = 2000;
                    SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
                    boolean isRoot = prefs.getBoolean("is_root", false);
                    if (!isRoot) {
                        File zoronDir = new File(getFilesDir(), "zoron");
                        File profileFile = new File(zoronDir, "profile.txt");
                        String mode = "balanced";
                        if (profileFile.exists()) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                                String m = br.readLine();
                                if (m != null) mode = m.trim();
                            } catch (Exception ignored) {}
                        }
                        if ("deep".equals(mode) || "hibernation".equals(mode) || "nightwatch".equals(mode)) {
                            delay = 10000; // 10s evaluation loop during battery saving profiles
                        }
                    }
                    handler.postDelayed(this, delay);
                }
            };
            handler.post(autopilotTask);
        }
        return START_STICKY;
    }

    private void evaluateAndSwitchMode() {
        // 1. Read autopilot enabled state from preferences
        SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean autopilotEnabled = prefs.getBoolean("autopilot_enabled", false);
        boolean isRoot = prefs.getBoolean("is_root", false);

        // 2. Get Battery Level & Charging State
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        boolean isCharging = false;
        int batteryLevel = 50;
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            isCharging = bm.isCharging();
        }

        // 3. Get Foreground App
        String fgApp = getForegroundApp();
        
        // 4. Universal Video Playback Detection (Debounced Audio)
        if (isAudioActive()) {
            audioActiveTicks++;
        } else {
            audioActiveTicks = 0;
        }
        boolean isVideoPlaying = isVideoPlaybackApp(fgApp) || (audioActiveTicks >= 3);

        // 5. Append battery logging if needed (every 10 minutes)
        runLocalBatteryLogging(batteryLevel);

        // 6. Write process report (every scan cycle or periodically)
        runLocalProcessReport(fgApp);

        // 7. Write power state
        runLocalPowerStateUpdate(isVideoPlaying);

        if (autopilotEnabled) {
            final int finalBatteryLevel = batteryLevel;
            final boolean finalIsCharging = isCharging;
            final String finalFgApp = fgApp;
            final boolean finalIsVideoPlaying = isVideoPlaying;
            final boolean finalIsRoot = isRoot;

            new Thread(() -> {
                String targetMode = "balanced";
                boolean ruleMatched = false;
                
                try {
                    com.zoron.whyred.data.ZoronDatabase db = com.zoron.whyred.data.ZoronDatabase.getDatabase(ZoronAutopilotService.this);
                    java.util.List<com.zoron.whyred.data.RuleEntity> rules = db.ruleDao().getEnabledRules();
                    
                    if (rules != null) {
                        for (com.zoron.whyred.data.RuleEntity rule : rules) {
                            boolean conditionMet = false;
                            if ("BATTERY_BELOW".equals(rule.conditionType)) {
                                if (finalBatteryLevel < Integer.parseInt(rule.conditionValue)) conditionMet = true;
                            } else if ("CHARGING".equals(rule.conditionType)) {
                                if (finalIsCharging) conditionMet = true;
                            } else if ("APP_FOREGROUND".equals(rule.conditionType) && finalFgApp != null) {
                                if (finalFgApp.contains(rule.conditionValue.toLowerCase())) conditionMet = true;
                            }
                            
                            if (conditionMet && "SET_MODE".equals(rule.actionType)) {
                                targetMode = rule.actionValue;
                                ruleMatched = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {}

                if (!ruleMatched) {
                    String predictedMode = learningManager.getPredictedMode(finalFgApp);
                    if (predictedMode != null) {
                        targetMode = predictedMode;
                    } else if (finalFgApp == null || finalFgApp.isEmpty()) {
                        targetMode = "deep";
                    } else if (finalIsVideoPlaying) {
                        targetMode = "video";
                    } else if (finalFgApp.contains("pubg") || finalFgApp.contains("mihoyo") || finalFgApp.contains("game") || finalFgApp.contains("roblox") || finalFgApp.contains("epicgames")) {
                        targetMode = "burst";
                    } else if (finalFgApp.contains("launcher") || finalFgApp.contains("systemui")) {
                        targetMode = "deep";
                    } else if (finalBatteryLevel < 20 && !finalIsCharging) {
                        targetMode = "nightwatch";
                    } else if (finalIsCharging) {
                        targetMode = "balanced";
                    } else {
                        targetMode = "balanced";
                    }
                }

                if (isVideoBoostActive && finalIsRoot) {
                    Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
                    isVideoBoostActive = false;
                }

                long now = System.currentTimeMillis();
                if (flapLockExpireTime > now) {
                    targetMode = flapLockMode;
                } else {
                    if (!targetMode.equals(lastMode)) {
                        long timeSinceLastSwitch = now - lastSwitchTime;
                        if (timeSinceLastSwitch < 20000) {
                            modeFlapCount++;
                            if (modeFlapCount >= 3) {
                                flapLockMode = "balanced";
                                if ("burst".equals(targetMode) || "video".equals(targetMode)) flapLockMode = targetMode;
                                flapLockExpireTime = now + 120000;
                                modeFlapCount = 0;
                                targetMode = flapLockMode;
                            }
                        } else {
                            modeFlapCount = 0;
                        }
                    }
                }

                if (!targetMode.equals(lastMode)) {
                    lastSwitchTime = now;
                    String finalTargetMode = targetMode;
                    if (finalIsRoot) {
                        Shell.cmd("cat /data/local/tmp/zoron/profile.txt").submit(out -> {
                            if (out.isSuccess() && !out.getOut().isEmpty()) {
                                String current = out.getOut().get(0).trim();
                                if (!current.equals(finalTargetMode)) {
                                    applyZoronMode(finalTargetMode);
                                }
                                lastMode = finalTargetMode;
                            } else {
                                applyZoronMode(finalTargetMode);
                                lastMode = finalTargetMode;
                            }
                        });
                    } else {
                        File zoronDir = new File(getFilesDir(), "zoron");
                        File profileFile = new File(zoronDir, "profile.txt");
                        String current = "";
                        if (profileFile.exists()) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                                current = br.readLine();
                                if (current != null) current = current.trim();
                            } catch (Exception ignored) {}
                        }
                        if (!finalTargetMode.equals(current)) {
                            applyZoronMode(finalTargetMode);
                        }
                        lastMode = finalTargetMode;
                    }
                }
            }).start();
        } else {
            // Manual Mode: Keep manual profile active, but apply temporary dynamic video boost
            lastMode = ""; // Reset autopilot state

            if (isVideoPlaying) {
                if (!isVideoBoostActive) {
                    if (isRoot) {
                        Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_on").exec();
                    } else {
                        applyNonRootZoronMode("video");
                    }
                    isVideoBoostActive = true;
                }
            } else {
                if (isVideoBoostActive) {
                    if (isRoot) {
                        Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
                    } else {
                        // Restore manual mode when video playback stops
                        File zoronDir = new File(getFilesDir(), "zoron");
                        File profileFile = new File(zoronDir, "profile.txt");
                        String manualMode = "balanced";
                        if (profileFile.exists()) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                                String m = br.readLine();
                                if (m != null) manualMode = m.trim();
                            } catch (Exception ignored) {}
                        }
                        applyNonRootZoronMode(manualMode);
                    }
                    isVideoBoostActive = false;
                } else {
                    // Monitor manual overrides to train AdaptiveLearningManager
                    File zoronDir = new File(getFilesDir(), "zoron");
                    File profileFile = new File(zoronDir, "profile.txt");
                    String currentManualMode = "balanced";
                    if (profileFile.exists()) {
                        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                            String m = br.readLine();
                            if (m != null) currentManualMode = m.trim();
                        } catch (Exception ignored) {}
                    }
                    if (!currentManualMode.equals(lastDetectedManualMode)) {
                        if (!lastDetectedManualMode.isEmpty() && fgApp != null && !fgApp.isEmpty()) {
                            learningManager.registerOverride(fgApp, currentManualMode);
                        }
                        lastDetectedManualMode = currentManualMode;
                    }
                }
            }
        }
    }

    private boolean isAudioActive() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return am != null && am.isMusicActive();
    }

    private boolean isVideoPlaybackApp(String fgApp) {
        if (fgApp == null || fgApp.isEmpty()) return false;
        String app = fgApp.toLowerCase();
        
        return app.contains("youtube") ||
               app.contains("netflix") ||
               app.contains("instagram") ||
               app.contains("tiktok") ||
               app.contains("musically") ||
               app.contains("twitch") ||
               app.contains("disney") ||
               app.contains("hotstar") ||
               app.contains("primevideo") ||
               app.contains("facebook") ||
               app.contains("snapchat") ||
               app.contains("vlc") ||
               app.contains("videoplayer") ||
               app.contains("mxtech") ||
               app.contains("video") ||
               app.contains("player") ||
               app.contains("gallery") ||
               app.contains("photos") ||
               app.contains("hulu") ||
               app.contains("hbo") ||
               app.contains("plex") ||
               app.contains("mxplayer") ||
               app.contains("kodi");
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
        SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        if (isRoot) {
            Shell.cmd(
                "sh /system/bin/zoron_fastpath.sh set_mode " + mode,
                "sh /system/bin/zoron_engine " + mode + " &"
            ).exec();
        } else {
            applyNonRootZoronMode(mode);
        }
    }

    private void applyNonRootZoronMode(String mode) {
        // Save current mode to local profile.txt
        try {
            File zoronDir = new File(getFilesDir(), "zoron");
            if (!zoronDir.exists()) zoronDir.mkdirs();
            File profileFile = new File(zoronDir, "profile.txt");
            try (FileWriter fw = new FileWriter(profileFile)) {
                fw.write(mode);
            }
            
            // Log the change
            File logFile = new File(zoronDir, "log.txt");
            try (FileWriter logFw = new FileWriter(logFile, true)) {
                String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                logFw.write("[" + timestamp + "] [ENGINE] Non-Root applied mode: " + mode.toUpperCase() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Apply optimizations based on mode
        boolean hasWriteSettings = Settings.System.canWrite(this);

        switch (mode.toLowerCase()) {
            case "balanced":
                // 1. Sync: Enabled
                setSystemSyncEnabled(true);
                // 2. Brightness & Timeout & Haptics & Touch sounds
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);
                        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;

            case "deep":
                // 1. Sync: Disabled
                setSystemSyncEnabled(false);
                // 2. Brightness & Timeout & Haptics & Touch sounds
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 76); // 30%
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;

            case "hibernation":
                // 1. Sync: Disabled
                setSystemSyncEnabled(false);
                // 2. Brightness & Timeout & Haptics & Touch sounds
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 51); // 20%
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;

            case "burst":
                // 1. Sync: Enabled
                setSystemSyncEnabled(true);
                // 2. Brightness & Timeout & Haptics & Touch sounds
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 300000); // 5m
                        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
                        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;

            case "nightwatch":
                // 1. Sync: Disabled
                setSystemSyncEnabled(false);
                // 2. Brightness & Timeout & Haptics & Touch sounds
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 25); // 10%
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                        Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                        Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 0);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;

            case "video":
                // 1. Sync: Disable during video playback to reduce background CPU cycles
                setSystemSyncEnabled(false);
                // 2. Brightness & Timeout
                if (hasWriteSettings) {
                    try {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 600000); // 10m
                    } catch (Exception e) { e.printStackTrace(); }
                }
                break;
        }
    }

    private void setSystemSyncEnabled(boolean enabled) {
        try {
            ContentResolver.setMasterSyncAutomatically(enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runLocalBatteryLogging(int batteryLevel) {
        long now = System.currentTimeMillis();
        // Log every 10 minutes (600,000 ms)
        if (now - lastBatteryLogTime >= 600000 || lastBatteryLogTime == 0) {
            lastBatteryLogTime = now;
            try {
                File zoronDir = new File(getFilesDir(), "zoron");
                if (!zoronDir.exists()) zoronDir.mkdirs();
                File csvFile = new File(zoronDir, "battery.csv");
                File profileFile = new File(zoronDir, "profile.txt");
                String mode = "balanced";
                if (profileFile.exists()) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                        String m = br.readLine();
                        if (m != null) mode = m.trim();
                    }
                }
                
                try (FileWriter fw = new FileWriter(csvFile, true)) {
                    fw.write((now / 1000) + "," + batteryLevel + "," + mode + ",NORMAL,1.8GHz,35.0\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void runLocalPowerStateUpdate(boolean isVideoPlaying) {
        try {
            File zoronDir = new File(getFilesDir(), "zoron");
            if (!zoronDir.exists()) zoronDir.mkdirs();
            File powerFile = new File(zoronDir, "power_state.txt");
            File profileFile = new File(zoronDir, "profile.txt");
            String mode = "balanced";
            if (profileFile.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                    String m = br.readLine();
                    if (m != null) mode = m.trim();
                }
            }

            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm != null && pm.isInteractive();

            String state;
            if (!isScreenOn) {
                if ("nightwatch".equals(mode)) {
                    state = "SLEEP_IDLE";
                } else if ("hibernation".equals(mode) || "deep".equals(mode)) {
                    state = "DEEP_IDLE";
                } else {
                    state = "LIGHT_IDLE";
                }
            } else {
                if (isVideoPlaying || "burst".equals(mode) || "video".equals(mode)) {
                    state = "HYPER_ACTIVE";
                } else {
                    state = "INTERACTIVE";
                }
            }

            try (FileWriter fw = new FileWriter(powerFile)) {
                fw.write(state + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runLocalProcessReport(String fgApp) {
        long now = System.currentTimeMillis();
        if (now - lastProcessReportTime >= 60000 || lastProcessReportTime == 0) {
            lastProcessReportTime = now;
            new Thread(() -> {
                try {
                    File zoronDir = new File(getFilesDir(), "zoron");
                    if (!zoronDir.exists()) zoronDir.mkdirs();
                    File reportFile = new File(zoronDir, "process_report.txt");
                    
                    File profileFile = new File(zoronDir, "profile.txt");
                    String mode = "balanced";
                    if (profileFile.exists()) {
                        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                            String m = br.readLine();
                            if (m != null) mode = m.trim();
                        }
                    }

                    File powerFile = new File(zoronDir, "power_state.txt");
                    String state = "UNKNOWN";
                    if (powerFile.exists()) {
                        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(powerFile))) {
                            String s = br.readLine();
                            if (s != null) state = s.trim();
                        }
                    }

                    UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                    if (usm == null) return;
                    
                    List<android.app.usage.UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 24 * 3600 * 1000, now);
                    
                    int tierS = 0, tierA = 0, tierB = 0, tierC = 0, tierD = 0;
                    StringBuilder listBuilder = new StringBuilder();

                    if (stats != null) {
                        List<android.app.usage.UsageStats> recentStats = new ArrayList<>();
                        for (android.app.usage.UsageStats s : stats) {
                            if (s.getLastTimeUsed() > 0 && s.getPackageName().contains(".")) {
                                recentStats.add(s);
                            }
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            recentStats.sort((s1, s2) -> Long.compare(s2.getLastTimeUsed(), s1.getLastTimeUsed()));
                        }

                        int count = 0;
                        for (android.app.usage.UsageStats s : recentStats) {
                            String pkg = s.getPackageName();
                            String tier = "C";
                            String restriction = "Monitored";

                            if (pkg.equals("com.android.systemui") || pkg.contains("launcher") || pkg.contains("inputmethod")) {
                                tier = "S";
                                restriction = "Unrestricted";
                                tierS++;
                            } else if (pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("discord") || pkg.contains("messaging") || pkg.contains("dialer") || pkg.contains("gmail")) {
                                tier = "A";
                                restriction = "Batched";
                                tierA++;
                            } else if (pkg.contains("gms") || pkg.contains("vending") || pkg.contains("maps") || pkg.contains("calendar")) {
                                tier = "B";
                                restriction = "Grouped";
                                tierB++;
                            } else if (now - s.getLastTimeUsed() > 4 * 3600 * 1000) {
                                tier = "D";
                                restriction = "Denied";
                                tierD++;
                            } else {
                                tierC++;
                                if ("deep".equals(mode) || "hibernation".equals(mode) || "nightwatch".equals(mode)) {
                                    restriction = "Restricted";
                                }
                            }

                            if (count < 20) {
                                listBuilder.append("[TIER ").append(tier).append("] ").append(pkg)
                                           .append(" - Score: ").append(pkg.equals(fgApp) ? "95" : "40")
                                           .append(" - ").append(restriction).append("\n");
                                count++;
                            }
                        }
                    }

                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
                    try (FileWriter fw = new FileWriter(reportFile)) {
                        fw.write("=== ZORON-X Process Report ===\n");
                        fw.write("Last updated: " + timestamp + "\n");
                        fw.write("Current Mode: " + mode.toUpperCase() + "\n");
                        fw.write("Current State: " + state + "\n");
                        fw.write("\n");
                        fw.write("--- Process Tiers ---\n");
                        fw.write("Tier S (critical):    " + tierS + "\n");
                        fw.write("Tier A (messaging):   " + tierA + "\n");
                        fw.write("Tier B (services):    " + tierB + "\n");
                        fw.write("Tier C (cached):      " + tierC + "\n");
                        fw.write("Tier D (dormant):     " + tierD + "\n");
                        fw.write("\n");
                        fw.write("--- Detailed Process List ---\n");
                        fw.write(listBuilder.toString());
                        fw.write("=== End Report ===\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && autopilotTask != null) {
            handler.removeCallbacks(autopilotTask);
        }
        if (isVideoBoostActive) {
            SharedPreferences prefs = getSharedPreferences("ZoronSettings", MODE_PRIVATE);
            boolean isRoot = prefs.getBoolean("is_root", false);
            if (isRoot) {
                Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
            } else {
                // Restore manual mode
                File zoronDir = new File(getFilesDir(), "zoron");
                File profileFile = new File(zoronDir, "profile.txt");
                String manualMode = "balanced";
                if (profileFile.exists()) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(profileFile))) {
                        String m = br.readLine();
                        if (m != null) manualMode = m.trim();
                    } catch (Exception ignored) {}
                }
                applyNonRootZoronMode(manualMode);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


package com.zoron.whyred;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.topjohnwu.superuser.Shell;

public class ZoronAutopilotService extends Service {
    private Handler handler;
    private Runnable autopilotTask;
    private String lastMode = "";
    private boolean isVideoBoostActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (autopilotTask == null) {
            autopilotTask = new Runnable() {
                @Override
                public void run() {
                    evaluateAndSwitchMode();
                    // Check every 2 seconds for high responsiveness (dynamic video scaling)
                    handler.postDelayed(this, 2000);
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
        
        // 4. Universal Video Playback Detection
        // Detects if the foreground app is a known video/social media app, OR if any audio output is active system-wide (covers unknown apps/players)
        boolean isVideoPlaying = isVideoPlaybackApp(fgApp) || isAudioActive();

        if (autopilotEnabled) {
            // Autopilot Mode: Active dynamic mode management
            String targetMode = "balanced";

            if (fgApp == null || fgApp.isEmpty()) {
                targetMode = "deep"; // Screen likely off or idle
            } else {
                if (isVideoPlaying) {
                    targetMode = "video"; // Video playback optimization mode
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

            // Turn off manual dynamic video boost if it was active
            if (isVideoBoostActive) {
                Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
                isVideoBoostActive = false;
            }

            if (!targetMode.equals(lastMode)) {
                String finalTargetMode = targetMode;
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
            }
        } else {
            // Manual Mode: Keep manual profile active, but apply temporary dynamic video boost
            lastMode = ""; // Reset autopilot state

            if (isVideoPlaying) {
                if (!isVideoBoostActive) {
                    Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_on").exec();
                    isVideoBoostActive = true;
                }
            } else {
                if (isVideoBoostActive) {
                    Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
                    isVideoBoostActive = false;
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
        // If the service is destroyed, ensure video boost is cleaned up
        if (isVideoBoostActive) {
            Shell.cmd("sh /system/bin/zoron_fastpath.sh video_boost_off").exec();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

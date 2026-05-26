package com.zoron.whyred;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.topjohnwu.superuser.Shell;

public class ZoronForegroundService extends Service {

    private static final String CHANNEL_ID = "zoron_active_mode";
    private static final int NOTIFICATION_ID = 1001;
    private Handler handler;
    private Runnable pollRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, buildNotification("Initializing..."),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, buildNotification("Initializing..."));
        }

        // Poll mode every 30 seconds
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                Shell.cmd("cat /data/local/tmp/zoron/profile.txt 2>/dev/null || echo 'Unknown'").submit(out -> {
                    String mode = "Unknown";
                    if (out.isSuccess() && !out.getOut().isEmpty()) {
                        mode = out.getOut().get(0).trim().toUpperCase();
                    }
                    if (mode.isEmpty()) mode = "Unknown";
                    updateNotification(mode);
                });
                handler.postDelayed(this, 30000);
            }
        };
        handler.post(pollRunnable);

        return START_STICKY;
    }

    private void updateNotification(String mode) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(mode));
        }
    }

    private Notification buildNotification(String mode) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ZORON-X Active")
                .setContentText("Mode: " + mode)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setColor(getColor(R.color.purple_primary))
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ZORON-X Active Mode",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows the currently active ZORON-X optimization mode");
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && pollRunnable != null) {
            handler.removeCallbacks(pollRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

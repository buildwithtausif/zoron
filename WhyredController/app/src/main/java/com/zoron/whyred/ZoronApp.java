package com.zoron.whyred;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

public class ZoronApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply Material 3 Dynamic Colors (Android 12+)
        DynamicColors.applyToActivitiesIfAvailable(this);

        // Set libsu global configuration
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10));
    }
}

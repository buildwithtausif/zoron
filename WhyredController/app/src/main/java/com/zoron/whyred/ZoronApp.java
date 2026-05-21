package com.zoron.whyred;

import android.app.Application;
import com.topjohnwu.superuser.Shell;

public class ZoronApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Set libsu global configuration
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10));
    }
}

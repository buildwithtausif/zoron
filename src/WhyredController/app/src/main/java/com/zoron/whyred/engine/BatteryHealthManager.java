package com.zoron.whyred.engine;

import android.content.Context;
import android.content.SharedPreferences;
import com.topjohnwu.superuser.Shell;
import com.zoron.whyred.data.CycleEntity;
import com.zoron.whyred.data.ZoronDatabase;

public class BatteryHealthManager {
    
    public static void collectCycleData(Context context, int batteryLevel, int tempAtCharge) {
        SharedPreferences prefs = context.getSharedPreferences("ZoronSettings", Context.MODE_PRIVATE);
        boolean isRoot = prefs.getBoolean("is_root", false);
        
        ZoronDatabase db = ZoronDatabase.getDatabase(context);
        
        if (isRoot) {
            // Read hardware level EFC
            Shell.cmd("cat /sys/class/power_supply/battery/cycle_count 2>/dev/null").submit(out -> {
                if (out.isSuccess() && !out.getOut().isEmpty()) {
                    try {
                        float cycles = Float.parseFloat(out.getOut().get(0).trim());
                        CycleEntity entity = new CycleEntity();
                        entity.timestamp = System.currentTimeMillis();
                        entity.partialCycle = cycles; // Assumes absolute cycles from kernel
                        entity.wearMultiplier = 1.0f;
                        entity.tempAtCharge = tempAtCharge;
                        new Thread(() -> db.cycleDao().insertCycle(entity)).start();
                    } catch (Exception e) {}
                }
            });
        } else {
            // Non root partial accumulation (called by Autopilot when battery level changes)
            float partial = 0.01f; // Assumes 1% charge = 0.01 cycle
            float wear = 1.0f;
            if (batteryLevel > 80) wear = 1.2f;
            if (tempAtCharge > 40) wear = 1.5f;
            
            CycleEntity entity = new CycleEntity();
            entity.timestamp = System.currentTimeMillis();
            entity.partialCycle = partial;
            entity.wearMultiplier = wear;
            entity.tempAtCharge = tempAtCharge;
            new Thread(() -> db.cycleDao().insertCycle(entity)).start();
        }
    }
}

package com.zoron.whyred.engine;

import android.content.Context;
import com.zoron.whyred.data.RecommendationEntity;
import com.zoron.whyred.data.ZoronDatabase;
import java.io.File;
import java.util.UUID;

public class AuditEngine {
    
    public static void runAudit(Context context) {
        new Thread(() -> {
            try {
                // Read local process report
                File zoronDir = new File(context.getFilesDir(), "zoron");
                File processFile = new File(zoronDir, "process_report.txt");
                
                if (processFile.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(processFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("[TIER C]") || line.contains("[TIER D]")) {
                            RecommendationEngine.generateRecommendation(context, 
                                "Background App Drain",
                                "An app is consuming battery in the background.",
                                "RESTRICT",
                                85);
                            break; 
                        }
                    }
                    br.close();
                }
                
                // Deep Sleep Audit 
                File powerFile = new File(zoronDir, "power_state.txt");
                if (powerFile.exists()) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(powerFile));
                    String state = br.readLine();
                    br.close();
                    if (state != null && state.trim().equals("LIGHT_IDLE")) {
                        RecommendationEngine.generateRecommendation(context, 
                            "Deep Sleep Missing",
                            "Device is not entering deep sleep. Consider Deep Mode.",
                            "SET_DEEP_MODE",
                            70);
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

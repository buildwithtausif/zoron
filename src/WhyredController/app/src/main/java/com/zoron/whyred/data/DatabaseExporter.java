package com.zoron.whyred.data;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class DatabaseExporter {
    public static void exportDatabaseToCsv(Context context, File targetDir) {
        ZoronDatabase db = ZoronDatabase.getDatabase(context);
        
        try {
            // Export Rules
            File rulesFile = new File(targetDir, "db_export_rules.csv");
            try (FileWriter fw = new FileWriter(rulesFile)) {
                fw.write("ruleId,conditionType,conditionValue,actionType,actionValue,priority,isEnabled\n");
                List<RuleEntity> rules = db.ruleDao().getAllRules();
                if (rules != null) {
                    for (RuleEntity r : rules) {
                        fw.write(String.format("%s,%s,%s,%s,%s,%d,%b\n", 
                            r.ruleId, r.conditionType, r.conditionValue, r.actionType, r.actionValue, r.priority, r.isEnabled));
                    }
                }
            }

            // Export Cycles
            File cyclesFile = new File(targetDir, "db_export_cycles.csv");
            try (FileWriter fw = new FileWriter(cyclesFile)) {
                fw.write("id,timestamp,partialCycle,wearMultiplier,tempAtCharge\n");
                List<CycleEntity> cycles = db.cycleDao().getAllCycles();
                if (cycles != null) {
                    for (CycleEntity c : cycles) {
                        fw.write(String.format("%d,%d,%f,%f,%d\n", 
                            c.id, c.timestamp, c.partialCycle, c.wearMultiplier, c.tempAtCharge));
                    }
                }
            }

            // Export Recommendations
            File recsFile = new File(targetDir, "db_export_recommendations.csv");
            try (FileWriter fw = new FileWriter(recsFile)) {
                fw.write("id,title,description,action,confidence,status,timestamp\n");
                List<RecommendationEntity> recs = db.recommendationDao().getAllRecommendations();
                if (recs != null) {
                    for (RecommendationEntity r : recs) {
                        fw.write(String.format("%s,%s,%s,%s,%d,%s,%d\n", 
                            r.id, r.title, r.description, r.action, r.confidence, r.status, r.timestamp));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

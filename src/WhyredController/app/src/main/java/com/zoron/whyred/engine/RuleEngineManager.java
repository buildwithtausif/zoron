package com.zoron.whyred.engine;

import android.content.Context;
import com.zoron.whyred.data.RuleEntity;
import com.zoron.whyred.data.ZoronDatabase;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class RuleEngineManager {
    
    public static void exportRulesForDaemon(Context context) {
        new Thread(() -> {
            ZoronDatabase db = ZoronDatabase.getDatabase(context);
            List<RuleEntity> rules = db.ruleDao().getEnabledRules();
            if (rules == null) return;
            
            try {
                File zoronDir = new File(context.getFilesDir(), "zoron");
                if (!zoronDir.exists()) zoronDir.mkdirs();
                
                File rulesFile = new File(zoronDir, "rules.csv");
                try (FileWriter fw = new FileWriter(rulesFile)) {
                    for (RuleEntity rule : rules) {
                        fw.write(String.format("%d,%s,%s,%s,%s\n",
                            rule.priority, rule.conditionType, rule.conditionValue, rule.actionType, rule.actionValue));
                    }
                }
                
                android.content.SharedPreferences prefs = context.getSharedPreferences("ZoronSettings", Context.MODE_PRIVATE);
                boolean isRoot = prefs.getBoolean("is_root", false);
                if (isRoot) {
                    com.topjohnwu.superuser.Shell.cmd("cp " + rulesFile.getAbsolutePath() + " /data/local/tmp/zoron/rules.csv").exec();
                    com.topjohnwu.superuser.Shell.cmd("chmod 666 /data/local/tmp/zoron/rules.csv").exec();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

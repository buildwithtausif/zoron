package com.zoron.whyred;

import android.content.Context;
import com.zoron.whyred.data.LearningEntity;
import com.zoron.whyred.data.ZoronDatabase;

public class AdaptiveLearningManager {
    private final ZoronDatabase db;
    private static final int CONFIDENCE_THRESHOLD = 3;
    private static final long DECAY_PERIOD_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    public AdaptiveLearningManager(Context context) {
        this.db = ZoronDatabase.getDatabase(context);
    }

    public void registerOverride(String packageName, String modeSelected) {
        if (packageName == null || packageName.isEmpty() || modeSelected == null) return;
        
        new Thread(() -> {
            LearningEntity entity = db.learningDao().getPattern(packageName);
            long now = System.currentTimeMillis();
            if (entity != null) {
                if (entity.preferredMode.equals(modeSelected)) {
                    entity.confidenceScore += 1;
                } else {
                    entity.confidenceScore -= 1;
                    if (entity.confidenceScore <= 0) {
                        entity.preferredMode = modeSelected;
                        entity.confidenceScore = 1;
                    }
                }
                entity.lastUpdated = now;
            } else {
                entity = new LearningEntity(packageName, modeSelected, 1, now);
            }
            db.learningDao().insertOrUpdate(entity);
        }).start();
    }

    public String getPredictedMode(String packageName) {
        if (packageName == null || packageName.isEmpty()) return null;
        
        LearningEntity entity = db.learningDao().getPattern(packageName);
        if (entity != null) {
            long now = System.currentTimeMillis();
            if (now - entity.lastUpdated > DECAY_PERIOD_MS) {
                entity.confidenceScore -= 1;
                entity.lastUpdated = now;
                db.learningDao().insertOrUpdate(entity);
            }
            if (entity.confidenceScore >= CONFIDENCE_THRESHOLD) {
                return entity.preferredMode;
            }
        }
        return null;
    }
}

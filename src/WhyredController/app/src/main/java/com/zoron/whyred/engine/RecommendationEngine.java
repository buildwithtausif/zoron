package com.zoron.whyred.engine;

import android.content.Context;
import com.zoron.whyred.data.RecommendationEntity;
import com.zoron.whyred.data.ZoronDatabase;
import java.util.UUID;

public class RecommendationEngine {

    public static void generateRecommendation(Context context, String title, String desc, String action, int confidence) {
        ZoronDatabase db = ZoronDatabase.getDatabase(context);
        
        RecommendationEntity rec = new RecommendationEntity();
        rec.id = UUID.randomUUID().toString();
        rec.title = title;
        rec.description = desc;
        rec.action = action;
        rec.confidence = confidence;
        rec.status = "NEW";
        rec.timestamp = System.currentTimeMillis();
        
        db.recommendationDao().insertRecommendation(rec);
    }
}

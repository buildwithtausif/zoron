package com.zoron.whyred.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "learning_patterns")
public class LearningEntity {
    @PrimaryKey
    @NonNull
    public String packageName;
    
    public String preferredMode;
    public int confidenceScore;
    public long lastUpdated;

    public LearningEntity(@NonNull String packageName, String preferredMode, int confidenceScore, long lastUpdated) {
        this.packageName = packageName;
        this.preferredMode = preferredMode;
        this.confidenceScore = confidenceScore;
        this.lastUpdated = lastUpdated;
    }
}

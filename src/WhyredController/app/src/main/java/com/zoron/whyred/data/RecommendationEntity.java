package com.zoron.whyred.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "recommendations")
public class RecommendationEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String title;
    public String description;
    public String action;
    public int confidence; // 0-100
    public String status; // NEW, SHOWN, APPLIED, DISMISSED
    public long timestamp;
}

package com.zoron.whyred.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "battery_cycles")
public class CycleEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;
    public float partialCycle; // e.g., 0.5
    public float wearMultiplier; // e.g., 1.5 if hot
    public int tempAtCharge; 
}

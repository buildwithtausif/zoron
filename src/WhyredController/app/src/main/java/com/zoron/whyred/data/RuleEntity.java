package com.zoron.whyred.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "rules")
public class RuleEntity {
    @PrimaryKey
    @NonNull
    public String ruleId = "";
    public String conditionType;
    public String conditionValue;
    public String actionType;
    public String actionValue;
    public int priority;
    public boolean isEnabled;
}

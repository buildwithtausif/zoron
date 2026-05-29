package com.zoron.whyred.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC")
    List<RuleEntity> getAllRules();
    
    @Query("SELECT * FROM rules WHERE isEnabled = 1 ORDER BY priority DESC")
    List<RuleEntity> getEnabledRules();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRule(RuleEntity rule);
    
    @Update
    void updateRule(RuleEntity rule);
    
    @Delete
    void deleteRule(RuleEntity rule);
}

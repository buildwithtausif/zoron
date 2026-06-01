package com.zoron.whyred.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LearningDao {
    @Query("SELECT * FROM learning_patterns")
    List<LearningEntity> getAllPatterns();

    @Query("SELECT * FROM learning_patterns WHERE packageName = :packageName LIMIT 1")
    LearningEntity getPattern(String packageName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(LearningEntity entity);

    @Query("DELETE FROM learning_patterns WHERE confidenceScore <= 0")
    void deleteOutdated();
}

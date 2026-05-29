package com.zoron.whyred.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE status != 'DISMISSED' AND status != 'APPLIED' ORDER BY confidence DESC")
    List<RecommendationEntity> getActiveRecommendations();

    @Query("SELECT * FROM recommendations ORDER BY timestamp DESC")
    List<RecommendationEntity> getAllRecommendations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecommendation(RecommendationEntity recommendation);
    
    @Update
    void updateRecommendation(RecommendationEntity recommendation);
}

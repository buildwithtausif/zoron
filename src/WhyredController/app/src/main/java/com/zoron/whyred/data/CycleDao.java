package com.zoron.whyred.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CycleDao {
    @Query("SELECT * FROM battery_cycles ORDER BY timestamp ASC")
    List<CycleEntity> getAllCycles();

    @Query("SELECT SUM(partialCycle * wearMultiplier) FROM battery_cycles")
    Float getTotalAccumulatedWear();

    @Insert
    void insertCycle(CycleEntity cycle);
}

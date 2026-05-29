package com.zoron.whyred.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {RuleEntity.class, CycleEntity.class, RecommendationEntity.class}, version = 1, exportSchema = false)
public abstract class ZoronDatabase extends RoomDatabase {
    public abstract RuleDao ruleDao();
    public abstract CycleDao cycleDao();
    public abstract RecommendationDao recommendationDao();

    private static volatile ZoronDatabase INSTANCE;

    public static ZoronDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ZoronDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ZoronDatabase.class, "zoron_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

package com.vaicomp.karkun.db;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryFilterDao {
    @Query("SELECT * FROM categoryfilter")
    List<CategoryFilter> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CategoryFilter... users);

    @Query("DELETE FROM categoryfilter")
    void nukeTable();

    @Query("SELECT * FROM categoryfilter WHERE isEnabled = 1")
    List<CategoryFilter> getEnabled();

    @Delete
    void delete(CategoryFilter user);
}

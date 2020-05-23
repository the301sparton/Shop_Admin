package com.vaicomp.karkun.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ShopItem.class, CategoryFilter.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract ShopItemDao shopItemDao();
    public abstract CategoryFilterDao categoryFilterDao();
}



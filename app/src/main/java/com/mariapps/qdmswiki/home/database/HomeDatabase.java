package com.mariapps.qdmswiki.home.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.mariapps.qdmswiki.home.model.ArticleModel;
import com.mariapps.qdmswiki.home.model.CategoryModel;
import com.mariapps.qdmswiki.home.model.DocumentModel;
import com.mariapps.qdmswiki.home.model.TagModel;

@Database(entities = {DocumentModel.class, ArticleModel.class, CategoryModel.class, TagModel.class}, version = 1,exportSchema = false)
public abstract class HomeDatabase extends RoomDatabase {

    private static volatile HomeDatabase INSTANCE;

    public abstract HomeDao homeDao();

    public static HomeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HomeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            HomeDatabase.class, "HomeDb.db")
                            .build();
                }
            }
        }
        return INSTANCE;

    }
}
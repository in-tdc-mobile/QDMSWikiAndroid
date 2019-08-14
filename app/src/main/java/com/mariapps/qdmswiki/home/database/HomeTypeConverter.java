package com.mariapps.qdmswiki.home.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mariapps.qdmswiki.home.model.ArticleModel;
import com.mariapps.qdmswiki.home.model.CategoryModel;
import com.mariapps.qdmswiki.home.model.TagModel;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class HomeTypeConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static List<TagModel> tagEntityToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<TagModel>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String tagEntityToString(List<TagModel> tagModelList) {
        return gson.toJson(tagModelList);
    }

    @TypeConverter
    public static List<CategoryModel> categoryEntityToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<CategoryModel>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String categoryEntityToString(List<CategoryModel> categoryModelList) {
        return gson.toJson(categoryModelList);
    }

    @TypeConverter
    public static List<ArticleModel> articleEntityToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<ArticleModel>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String articleEntityToString(List<ArticleModel> articleModelList) {
        return gson.toJson(articleModelList);
    }

    @TypeConverter
    public static List<String> categoryIdsToList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<String>>() {}.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String categoryIdsToString(List<String> categoryIds) {
        return gson.toJson(categoryIds);
    }
}